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
public class InnerServiceNotSupported {

    private final LogRepository logRepository;

    // 明确指定 Propagation.NOT_SUPPORTED
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void notSupportedAction(String message, boolean shouldFail) {
        // 检查当前是否存在活动的事务 (预期这里应该总是 false，因为外部事务会被挂起)
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("  [InnerServiceNotSupported.notSupportedAction] 开始执行 ( NOT_SUPPORTED ). 当前是否存在事务? {}", actualTransactionActive);

        logRepository.save(new LogEntry("InnerNotSupported Log: " + message));
        log.info("  [InnerServiceNotSupported.notSupportedAction] 内部日志已保存 (非事务性).");

        if (shouldFail) {
            log.warn("  [InnerServiceNotSupported.notSupportedAction] 即将抛出运行时异常 (非事务性)!");
            throw new RuntimeException("InnerServiceNotSupported 失败了!");
            // 因为是非事务性执行，即使这里抛异常，前面保存的日志也无法自动回滚！
        }
        log.info("  [InnerServiceNotSupported.notSupportedAction] 执行完毕 (非事务性).");
    }
}