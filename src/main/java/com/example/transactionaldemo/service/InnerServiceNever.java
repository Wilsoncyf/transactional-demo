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
public class InnerServiceNever {

    private final LogRepository logRepository;

    // 明确指定 Propagation.NEVER
    @Transactional(propagation = Propagation.NEVER)
    public void neverAction(String message) {
        // 这段代码只有在调用者 *没有* 提供事务时才会执行
        // 如果有事务，在方法入口处就会抛出 IllegalTransactionStateException
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("  [InnerServiceNever.neverAction] 开始执行 ( NEVER ). 当前是否存在事务? {}", actualTransactionActive); // 这里必定是 false

        logRepository.save(new LogEntry("InnerNever Log: " + message));
        log.info("  [InnerServiceNever.neverAction] 内部日志已保存 (非事务性).");

        log.info("  [InnerServiceNever.neverAction] 执行完毕.");
    }
}