package com.example.transactionaldemo.service;

import com.example.transactionaldemo.entity.LogEntry;
import com.example.transactionaldemo.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Slf4j
@RequiredArgsConstructor
public class InnerServiceNested {

    private final LogRepository logRepository;

    // Specify Propagation.NESTED
    @Transactional(propagation = Propagation.NESTED)
    public void nestedAction(String message, boolean shouldFail) {
        // This will execute within a nested transaction (using a savepoint)
        // if called from an existing transaction. Otherwise, like REQUIRED.
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("  [InnerServiceNested.nestedAction] Started (NESTED). Active Tx? {}, Tx Name: {}", actualTransactionActive, currentTransactionName);

        logRepository.save(new LogEntry("InnerNested Log: " + message));
        log.info("  [InnerServiceNested.nestedAction] Inner log saved within nested scope.");

        if (shouldFail) {
            log.warn("  [InnerServiceNested.nestedAction] Simulating failure within nested transaction!");
            throw new RuntimeException("InnerServiceNested failed!");
            // This exception will cause a rollback *to the savepoint* created for this nested transaction.
        }
        log.info("  [InnerServiceNested.nestedAction] Completed successfully (nested scope).");
        // If successful, changes are part of the outer transaction, pending its commit.
    }
}