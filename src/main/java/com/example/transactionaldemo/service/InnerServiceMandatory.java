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
public class InnerServiceMandatory {

    private final LogRepository logRepository;

    // 明确指定 Propagation.MANDATORY
    @Transactional(propagation = Propagation.MANDATORY)
    public void mandatoryAction(String message) {
        // 这段代码只有在调用者提供了事务时才会执行
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        log.info("  [InnerServiceMandatory.mandatoryAction] 开始执行 ( MANDATORY ). 当前是否存在事务? {}", actualTransactionActive); // 这里必定是 true

        logRepository.save(new LogEntry("InnerMandatory Log: " + message));
        log.info("  [InnerServiceMandatory.mandatoryAction] 内部日志已保存 (在现有事务中).");

        // 这里我们不添加失败逻辑，主要演示能否被调用
        log.info("  [InnerServiceMandatory.mandatoryAction] 执行完毕.");
    }
}