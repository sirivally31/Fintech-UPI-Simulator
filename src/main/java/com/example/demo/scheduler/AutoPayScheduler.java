package com.example.demo.scheduler;

import com.example.demo.entity.AutoPay;
import com.example.demo.entity.AutoPayStatus;
import com.example.demo.repository.AutoPayRepository;
import com.example.demo.service.AutoPayService;
import com.example.demo.service.DistributedLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Enterprise background scheduler executing due AutoPay mandates using
 * Redis distributed locking to prevent duplicate concurrent executions across cluster nodes.
 */
@Component
public class AutoPayScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoPayScheduler.class);

    private final AutoPayRepository autoPayRepository;
    private final AutoPayService autoPayService;
    private final DistributedLockService distributedLockService;

    public AutoPayScheduler(AutoPayRepository autoPayRepository,
                           AutoPayService autoPayService,
                           DistributedLockService distributedLockService) {
        this.autoPayRepository = autoPayRepository;
        this.autoPayService = autoPayService;
        this.distributedLockService = distributedLockService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void processDueAutoPays() {
        log.info("AutoPay Scheduler triggered - checking due mandates");

        List<AutoPay> dueMandates = autoPayRepository
                .findByStatusAndActiveTrueAndNextExecutionTimeLessThanEqual(AutoPayStatus.ACTIVE, LocalDateTime.now());

        if (dueMandates.isEmpty()) {
            log.debug("No due AutoPay mandates found for execution.");
            return;
        }

        log.info("Found [{}] due AutoPay mandates for execution", dueMandates.size());

        for (AutoPay mandate : dueMandates) {
            String lockKey = "autopay:exec:" + mandate.getId();
            String lockValue = UUID.randomUUID().toString();

            // Try acquiring 60-second distributed lock for this mandate execution
            boolean lockAcquired = distributedLockService.acquireLock(lockKey, lockValue, 60);
            if (!lockAcquired) {
                log.info("Skipping AutoPay mandate [{}] - lock currently held by another worker", mandate.getMandateReference());
                continue;
            }

            try {
                autoPayService.executeDueAutoPay(mandate);
            } catch (Exception ex) {
                log.error("Unhandled error during scheduled execution of AutoPay mandate [{}]", mandate.getMandateReference(), ex);
            } finally {
                distributedLockService.releaseLock(lockKey, lockValue);
            }
        }
    }
}
