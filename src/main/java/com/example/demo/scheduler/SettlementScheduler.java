package com.example.demo.scheduler;

import com.example.demo.service.DistributedLockService;
import com.example.demo.service.SettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Enterprise background scheduler executing daily settlement batch clearing and hourly
 * ledger reconciliation using Redis distributed locks to prevent cluster node race conditions.
 */
@Component
public class SettlementScheduler {

    private static final Logger log = LoggerFactory.getLogger(SettlementScheduler.class);

    private final SettlementService settlementService;
    private final DistributedLockService distributedLockService;

    public SettlementScheduler(SettlementService settlementService,
                               DistributedLockService distributedLockService) {
        this.settlementService = settlementService;
        this.distributedLockService = distributedLockService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void runDailySettlement() {
        log.info("Settlement Scheduler triggered - initiating daily batch clearing");

        String lockKey = "settlement:exec:daily";
        String lockValue = UUID.randomUUID().toString();

        boolean acquired = distributedLockService.acquireLock(lockKey, lockValue, 300);
        if (!acquired) {
            log.info("Skipping daily settlement batch - lock currently held by another worker node");
            return;
        }

        try {
            settlementService.processDailySettlements();
        } catch (Exception e) {
            log.error("Unhandled error during daily settlement processing", e);
        } finally {
            distributedLockService.releaseLock(lockKey, lockValue);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void runHourlyReconciliation() {
        log.info("Settlement Scheduler triggered - initiating hourly ledger reconciliation");

        String lockKey = "settlement:exec:reconciliation";
        String lockValue = UUID.randomUUID().toString();

        boolean acquired = distributedLockService.acquireLock(lockKey, lockValue, 120);
        if (!acquired) {
            log.info("Skipping hourly reconciliation - lock currently held by another worker node");
            return;
        }

        try {
            settlementService.reconcileLedger();
        } catch (Exception e) {
            log.error("Unhandled error during hourly ledger reconciliation", e);
        } finally {
            distributedLockService.releaseLock(lockKey, lockValue);
        }
    }
}
