package com.example.transactionaldemo.service;

import com.example.transactionaldemo.entity.LogEntry;
import com.example.transactionaldemo.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.IllegalTransactionStateException; // 需要捕获的异常
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OuterServiceNever {

    private final LogRepository logRepository;
    private final InnerServiceNever innerServiceNever; // 注入 Never Service

    // === 场景 11: 外部 REQUIRED, 内部 NEVER ===
    @Transactional // 默认 REQUIRED, 启动事务 T1
    public void callInnerNever_OuterRequired() {
        log.info("[OuterServiceNever.Scenario11] 开始执行 (外部 REQUIRED)...");
        logRepository.save(new LogEntry("OuterNever Log before calling inner"));
        log.info("[OuterServiceNever.Scenario11] 外部日志 (before) 已保存 (事务 T1).");

        try {
            // 调用 InnerServiceNever 的 neverAction 方法
            // !! 关键：因为当前方法有事务 T1，InnerServiceNever (NEVER) 会立即抛出 IllegalTransactionStateException
            log.info("[OuterServiceNever.Scenario11] 即将调用 InnerServiceNever...");
            innerServiceNever.neverAction("Data from OuterNever (REQUIRED)");
            // 如果能执行到这里，说明 NEVER 的行为不符合预期
             log.warn("[OuterServiceNever.Scenario11] InnerServiceNever 调用似乎成功了？(预期应该抛异常)");
        } catch (IllegalTransactionStateException e) {
            // 捕获预期的异常
            log.error("[OuterServiceNever.Scenario11] 成功捕获到预期的异常 (因为外部有事务): {}", e.getMessage());
            // 这里我们捕获了异常，可以选择让外部事务 T1 继续或失败
            // 为了演示，我们让 T1 继续并提交 (不重抛异常)
             log.info("[OuterServiceNever.Scenario11] 即使内部调用失败，外部事务 T1 仍将尝试提交...");
        } catch (RuntimeException e) {
            // 捕获其他可能的运行时异常
             log.error("[OuterServiceNever.Scenario11] 捕获到意外的运行时异常: {}", e.getMessage());
             throw e; // 其他异常通常应该重抛以回滚 T1
        }

        logRepository.save(new LogEntry("OuterNever Log after calling inner"));
        log.info("[OuterServiceNever.Scenario11] 外部日志 (after) 已保存 (事务 T1).");
        log.info("[OuterServiceNever.Scenario11] 执行完毕 (外部 REQUIRED).");
        // T1 尝试提交 (如果 IllegalTransactionStateException 被捕获且未重抛)
    }

    // === 场景 12: 外部无事务, 内部 NEVER ===
    // 这个方法 *没有* @Transactional 注解
    public void callInnerNever_OuterNonTransactional() {
        log.info("[OuterServiceNever.Scenario12] 开始执行 (外部无事务)...");
        try {
            // 调用 InnerServiceNever 的 neverAction 方法
            // 因为当前方法没有事务，InnerServiceNever (NEVER) 会正常以非事务方式执行
            innerServiceNever.neverAction("Data from Non-Transactional OuterNever");
            log.info("[OuterServiceNever.Scenario12] InnerServiceNever 调用成功 (非事务性).");
        } catch (RuntimeException e) {
            // 如果 innerServiceNever 内部发生其他异常，会在这里捕获
            log.error("[OuterServiceNever.Scenario12] 捕获到来自 InnerServiceNever 的异常: {}", e.getMessage());
        }
        log.info("[OuterServiceNever.Scenario12] 执行完毕 (外部无事务).");
    }
}