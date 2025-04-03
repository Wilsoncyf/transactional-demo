package com.example.transactionaldemo.service;

import com.example.transactionaldemo.entity.LogEntry;
import com.example.transactionaldemo.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InnerServiceNewTx {

    private final LogRepository logRepository;

    // 明确指定 Propagation.REQUIRES_NEW
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNewAction(String message, boolean shouldFail) {
        log.info("  [InnerServiceNewTx.requiresNewAction] 开始执行 ( REQUIRES_NEW )...");
        logRepository.save(new LogEntry("InnerNewTx Log: " + message));
        log.info("  [InnerServiceNewTx.requiresNewAction] 内部日志已保存 (新事务).");

        if (shouldFail) {
            log.warn("  [InnerServiceNewTx.requiresNewAction] 即将抛出运行时异常 (新事务)!");
            throw new RuntimeException("InnerServiceNewTx 失败了!"); // 这将导致这个新事务 T2 回滚
        }
        log.info("  [InnerServiceNewTx.requiresNewAction] 执行完毕 (新事务).");
        // 如果正常结束，这个新事务 T2 会在这里提交
    }
}