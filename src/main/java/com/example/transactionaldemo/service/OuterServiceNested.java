package com.example.transactionaldemo.service;

import com.example.transactionaldemo.entity.LogEntry;
import com.example.transactionaldemo.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@Service
@Slf4j
@RequiredArgsConstructor
public class OuterServiceNested {

    private final LogRepository logRepository;
    private final InnerServiceNested innerServiceNested; // Inject Nested Service

    // === Scenario 13: Inner NESTED Fails, Outer REQUIRED Succeeds (Partial Rollback) ===
    @Transactional // Default REQUIRED, starts T1
    public void scenarioInnerFailOuterSuccess() {
        String outerTxName = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("[OuterServiceNested.Scenario13] Started (Outer REQUIRED). Tx Name: {}", outerTxName);
        logRepository.save(new LogEntry("OuterNested Log 1 (Scenario 13 - Before Inner)"));
        log.info("[OuterServiceNested.Scenario13] Outer log 1 saved (T1).");

        try {
            // Call the NESTED method, instructing it to fail
            // A savepoint will be created within T1 before this call
            innerServiceNested.nestedAction("Data for Scenario 13", true);
        } catch (RuntimeException e) {
            // Catch the exception from the nested method
            log.error("[OuterServiceNested.Scenario13] Caught exception from InnerServiceNested: {}. Outer transaction T1 continues.", e.getMessage());
            // !! Crucially, we CATCH the exception and DO NOT re-throw it.
            // The rollback only happened back to the savepoint. T1 continues.
        }

        // T1 continues execution after the savepoint rollback
        logRepository.save(new LogEntry("OuterNested Log 2 (Scenario 13 - After Inner Failed)"));
        log.info("[OuterServiceNested.Scenario13] Outer log 2 saved (T1).");
        log.info("[OuterServiceNested.Scenario13] Completing successfully (Outer REQUIRED).");
        // T1 commits here. Work before the savepoint and after the (rolled-back) nested call persists.
    }


    // === Scenario 14: Inner NESTED Succeeds, Outer REQUIRED Fails (Full Rollback) ===
    @Transactional // Default REQUIRED, starts T1
    public void scenarioInnerSuccessOuterFail() {
        String outerTxName = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("[OuterServiceNested.Scenario14] Started (Outer REQUIRED). Tx Name: {}", outerTxName);
        logRepository.save(new LogEntry("OuterNested Log 1 (Scenario 14 - Before Inner)"));
        log.info("[OuterServiceNested.Scenario14] Outer log 1 saved (T1).");

        try {
            // Call the NESTED method, instructing it to succeed
            // A savepoint is created, inner method runs, savepoint might be released.
            // Inner's changes are now part of T1.
            innerServiceNested.nestedAction("Data for Scenario 14", false);
            log.info("[OuterServiceNested.Scenario14] InnerServiceNested completed successfully.");
        } catch (RuntimeException e) {
            // This shouldn't happen in this scenario
            log.error("[OuterServiceNested.Scenario14] Unexpected exception from InnerServiceNested: {}", e.getMessage());
            throw e; // Re-throw unexpected errors
        }

        // Outer transaction T1 continues, but now encounters an error
        log.warn("[OuterServiceNested.Scenario14] Simulating failure in outer transaction AFTER inner completed!");
        throw new RuntimeException("OuterServiceNested failed AFTER inner completed!");
        // This exception causes the *entire* transaction T1 to roll back,
        // including work done by the nested method.
    }
}