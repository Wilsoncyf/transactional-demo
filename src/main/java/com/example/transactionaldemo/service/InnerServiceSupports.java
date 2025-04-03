package com.example.transactionaldemo.service;

import com.example.transactionaldemo.entity.LogEntry;
import com.example.transactionaldemo.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager; // 用于检查当前是否存在事务

@Service
@Slf4j
@RequiredArgsConstructor
public class InnerServiceSupports {

    private final LogRepository logRepository;

    // 明确指定 Propagation.SUPPORTS
    @Transactional(propagation = Propagation.SUPPORTS)
    public void supportsAction(String message, boolean shouldFail) {
        // 检查当前是否存在活动的事务
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("  [InnerServiceSupports.supportsAction] 开始执行 ( SUPPORTS ). 当前是否存在事务? {}", actualTransactionActive);

        logRepository.save(new LogEntry("InnerSupports Log: " + message));
        log.info("  [InnerServiceSupports.supportsAction] 内部日志已保存.");

        if (shouldFail) {
            log.warn("  [InnerServiceSupports.supportsAction] 即将抛出运行时异常!");
            throw new RuntimeException("InnerServiceSupports 失败了!");
            // 如果当前有事务 (actualTransactionActive=true)，这个异常会导致该事务回滚
            // 如果当前没有事务 (actualTransactionActive=false)，这个异常不会导致任何事务回滚（因为没有事务）
            // 但已经保存的 "InnerSupports Log" 在非事务模式下可能已提交，无法回滚！
        }
        log.info("  [InnerServiceSupports.supportsAction] 执行完毕.");
    }
}