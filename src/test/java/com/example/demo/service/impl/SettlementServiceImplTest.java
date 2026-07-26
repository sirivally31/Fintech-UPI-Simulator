package com.example.demo.service.impl;

import com.example.demo.dto.ReconciliationSummaryResponse;
import com.example.demo.dto.SettlementBatchResponse;
import com.example.demo.entity.*;
import com.example.demo.metrics.BusinessMetricsService;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.*;
import com.example.demo.service.OutboxService;
import com.example.demo.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceImplTest {

    @Mock
    private SettlementBatchRepository settlementBatchRepository;

    @Mock
    private SettlementEntryRepository settlementEntryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private OutboxService outboxService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private BusinessMetricsService businessMetricsService;

    @InjectMocks
    private SettlementServiceImpl settlementService;

    private SettlementBatch batch;
    private UUID batchId;
    private Transaction txn;

    @BeforeEach
    void setUp() {
        batchId = UUID.randomUUID();
        batch = new SettlementBatch(
                batchId, "SET2026072619001", SettlementType.UPI_TRANSFER, SettlementStatus.COMPLETED,
                5, new BigDecimal("5000.00"), new BigDecimal("5000.00"), new BigDecimal("5000.00"),
                0, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        txn = new Transaction();
        txn.setId(100L);
        txn.setTransactionReference("TXN998877");
        txn.setAmount(new BigDecimal("1000.00"));
        txn.setStatus(TransactionStatus.SUCCESS);
    }

    @Test
    @DisplayName("Process Daily Settlements - Successful Clearing")
    void testProcessDailySettlements_Success() {
        when(transactionRepository.findAll()).thenReturn(List.of(txn));
        when(settlementBatchRepository.save(any(SettlementBatch.class))).thenAnswer(i -> {
            SettlementBatch b = i.getArgument(0);
            b.setId(batchId);
            return b;
        });

        SettlementBatchResponse response = settlementService.processDailySettlements();

        assertNotNull(response);
        assertEquals(SettlementStatus.COMPLETED, response.getStatus());
        assertEquals(1, response.getTotalRecords());

        verify(outboxService).saveOutboxEvent(any(), eq("SETTLEMENT"), anyLong(), eq("SETTLEMENT_COMPLETED"), anyString(), any());
        verify(eventPublisher).publishSettlementCompleted(any());
        verify(redisCacheService).delete("settlement:summary");
    }

    @Test
    @DisplayName("Reconcile Ledger - Balanced Totals")
    void testReconcileLedger_Success() {
        when(transactionRepository.findAll()).thenReturn(List.of(txn));
        when(bankAccountRepository.findAll()).thenReturn(Collections.emptyList());
        when(settlementBatchRepository.findByStatus(SettlementStatus.PENDING)).thenReturn(Collections.emptyList());
        when(settlementBatchRepository.findByStatus(SettlementStatus.FAILED)).thenReturn(Collections.emptyList());

        ReconciliationSummaryResponse summary = settlementService.reconcileLedger();

        assertNotNull(summary);
        assertEquals("RECONCILED", summary.getReconciliationStatus());
        assertEquals(0, summary.getNetBalanceDifference().compareTo(BigDecimal.ZERO));

        verify(outboxService).saveOutboxEvent(any(), eq("SETTLEMENT"), anyLong(), eq("SETTLEMENT_RECONCILED"), anyString(), any());
        verify(eventPublisher).publishSettlementReconciled(any());
    }

    @Test
    @DisplayName("Reverse Settlement Batch - Success")
    void testReverseSettlementBatch_Success() {
        when(settlementBatchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(settlementBatchRepository.save(any(SettlementBatch.class))).thenReturn(batch);

        SettlementBatchResponse response = settlementService.reverseSettlementBatch(batchId);

        assertNotNull(response);
        assertEquals(SettlementStatus.REVERSED, batch.getStatus());

        verify(outboxService).saveOutboxEvent(any(), eq("SETTLEMENT"), anyLong(), eq("SETTLEMENT_REVERSED"), anyString(), any());
        verify(eventPublisher).publishSettlementReversed(any());
    }
}
