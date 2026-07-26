package com.example.demo.service.impl;

import com.example.demo.dto.MerchantQrResponse;
import com.example.demo.dto.QrGenerateRequest;
import com.example.demo.dto.QrScanRequest;
import com.example.demo.dto.QrScanResponse;
import com.example.demo.entity.Merchant;
import com.example.demo.entity.MerchantQr;
import com.example.demo.entity.QRType;
import com.example.demo.events.QrCreatedEvent;
import com.example.demo.exception.InvalidQrException;
import com.example.demo.exception.MerchantNotFoundException;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.MerchantQrRepository;
import com.example.demo.repository.MerchantRepository;
import com.example.demo.service.QrService;
import com.example.demo.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of QrService managing QR token generation, UPI URI formatting,
 * caching in Redis, publishing Kafka events, and verifying QR scans.
 */
@Service
public class QrServiceImpl implements QrService {

    private static final Logger log = LoggerFactory.getLogger(QrServiceImpl.class);
    private static final String REDIS_QR_KEY_PREFIX = "qr:token:";

    @Value("${app.qr.default-dynamic-expiry-minutes:15}")
    private long defaultDynamicExpiryMinutes;

    private final MerchantRepository merchantRepository;
    private final MerchantQrRepository merchantQrRepository;
    private final RedisCacheService redisCacheService;
    private final EventPublisher eventPublisher;

    public QrServiceImpl(MerchantRepository merchantRepository,
                         MerchantQrRepository merchantQrRepository,
                         RedisCacheService redisCacheService,
                         EventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.merchantQrRepository = merchantQrRepository;
        this.redisCacheService = redisCacheService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public MerchantQrResponse generateQr(QrGenerateRequest request) {
        log.info("Generating [{}] QR code for merchant ID [{}]", request.getType(), request.getMerchantId());

        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found with ID: " + request.getMerchantId()));

        if (!Boolean.TRUE.equals(merchant.getActive())) {
            throw new InvalidQrException("Cannot generate QR code for inactive merchant ID: " + request.getMerchantId());
        }

        if (request.getType() == QRType.DYNAMIC) {
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidQrException("Dynamic QR generation requires a positive payment amount.");
            }
        }

        String qrToken = "qr_" + UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = null;
        long cacheTtlSeconds = 86400 * 30; // 30 days for static QR cache default

        if (request.getType() == QRType.DYNAMIC) {
            long minutes = (request.getExpiryMinutes() != null && request.getExpiryMinutes() > 0)
                    ? request.getExpiryMinutes() : defaultDynamicExpiryMinutes;
            expiryTime = now.plusMinutes(minutes);
            cacheTtlSeconds = minutes * 60;
        }

        MerchantQr merchantQr = new MerchantQr();
        merchantQr.setMerchant(merchant);
        merchantQr.setQrToken(qrToken);
        merchantQr.setAmount(request.getAmount());
        merchantQr.setDescription(request.getDescription());
        merchantQr.setType(request.getType());
        merchantQr.setUsed(false);
        merchantQr.setExpiryTime(expiryTime);
        merchantQr.setCreatedAt(now);

        MerchantQr savedQr = merchantQrRepository.save(merchantQr);
        String paymentUri = buildUpiPaymentUri(merchant.getUpiId(), merchant.getBusinessName(), savedQr.getAmount(), savedQr.getDescription());
        
        MerchantQrResponse response = mapToResponse(savedQr, paymentUri);

        // Cache in Redis
        redisCacheService.save(REDIS_QR_KEY_PREFIX + qrToken, response, cacheTtlSeconds, TimeUnit.SECONDS);

        // Publish Kafka Event
        publishQrCreatedEvent(savedQr, merchant);

        log.info("Successfully generated [{}] QR token [{}] for merchant [{}]", savedQr.getType(), qrToken, merchant.getMerchantCode());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantQrResponse getQrByToken(String qrToken) {
        String cacheKey = REDIS_QR_KEY_PREFIX + qrToken;
        MerchantQrResponse cached = redisCacheService.find(cacheKey, MerchantQrResponse.class);
        if (cached != null) {
            log.info("Retrieved QR token [{}] from Redis cache", qrToken);
            return cached;
        }

        MerchantQr merchantQr = merchantQrRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new InvalidQrException("Invalid or non-existent QR token: " + qrToken));

        String paymentUri = buildUpiPaymentUri(
                merchantQr.getMerchant().getUpiId(),
                merchantQr.getMerchant().getBusinessName(),
                merchantQr.getAmount(),
                merchantQr.getDescription()
        );

        MerchantQrResponse response = mapToResponse(merchantQr, paymentUri);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public QrScanResponse scanQr(QrScanRequest request) {
        String qrToken = request.getQrToken();
        log.info("Processing scan request for QR token [{}]", qrToken);

        MerchantQr merchantQr = merchantQrRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new InvalidQrException("QR code invalid or not found with token: " + qrToken));

        Merchant merchant = merchantQr.getMerchant();
        if (!Boolean.TRUE.equals(merchant.getActive())) {
            throw new InvalidQrException("Scanned QR belongs to an inactive merchant.");
        }

        boolean isExpired = false;
        if (merchantQr.getType() == QRType.DYNAMIC && merchantQr.getExpiryTime() != null) {
            if (LocalDateTime.now().isAfter(merchantQr.getExpiryTime())) {
                isExpired = true;
            }
        }

        boolean isUsed = Boolean.TRUE.equals(merchantQr.getUsed());
        if (merchantQr.getType() == QRType.DYNAMIC && isUsed) {
            throw new InvalidQrException("Dynamic QR code has already been used.");
        }

        if (isExpired) {
            throw new InvalidQrException("Dynamic QR code has expired.");
        }

        String paymentUri = buildUpiPaymentUri(
                merchant.getUpiId(),
                merchant.getBusinessName(),
                merchantQr.getAmount(),
                merchantQr.getDescription()
        );

        return new QrScanResponse(
                qrToken,
                merchant.getId(),
                merchant.getMerchantName(),
                merchant.getBusinessName(),
                merchant.getUpiId(),
                merchantQr.getAmount(),
                merchantQr.getDescription(),
                merchantQr.getType(),
                true, // valid
                false, // expired
                isUsed,
                paymentUri,
                "QR Code valid and ready for payment processing"
        );
    }

    private String buildUpiPaymentUri(String upiId, String businessName, BigDecimal amount, String note) {
        StringBuilder uri = new StringBuilder("upi://pay?");
        uri.append("pa=").append(encode(upiId));
        uri.append("&pn=").append(encode(businessName));
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            uri.append("&am=").append(amount.setScale(2).toString());
        }
        uri.append("&cu=INR");
        if (note != null && !note.isBlank()) {
            uri.append("&tn=").append(encode(note));
        }
        return uri.toString();
    }

    private String encode(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private void publishQrCreatedEvent(MerchantQr merchantQr, Merchant merchant) {
        try {
            QrCreatedEvent event = QrCreatedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventTime(LocalDateTime.now())
                    .eventType("QR_CREATED")
                    .correlationId(UUID.randomUUID().toString())
                    .qrToken(merchantQr.getQrToken())
                    .merchantId(merchant.getId())
                    .merchantCode(merchant.getMerchantCode())
                    .upiId(merchant.getUpiId())
                    .amount(merchantQr.getAmount())
                    .type(merchantQr.getType().name())
                    .expiryTime(merchantQr.getExpiryTime())
                    .build();

            eventPublisher.publishQrCreated(event);
        } catch (Exception e) {
            log.error("Error publishing QR_CREATED event for token [{}]", merchantQr.getQrToken(), e);
        }
    }

    private MerchantQrResponse mapToResponse(MerchantQr qr, String paymentUri) {
        return new MerchantQrResponse(
                qr.getId(),
                qr.getMerchant().getId(),
                qr.getMerchant().getMerchantName(),
                qr.getMerchant().getBusinessName(),
                qr.getMerchant().getUpiId(),
                qr.getQrToken(),
                qr.getAmount(),
                qr.getDescription(),
                qr.getType(),
                qr.getUsed(),
                qr.getExpiryTime(),
                paymentUri,
                qr.getCreatedAt()
        );
    }
}
