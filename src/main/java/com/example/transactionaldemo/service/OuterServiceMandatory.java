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
public class OuterServiceMandatory {

    private final LogRepository logRepository;
    private final InnerServiceMandatory innerServiceMandatory; // 注入 Mandatory Service

    // === 场景 7: 外部 REQUIRED, 内部 MANDATORY ===
    @Transactional // 默认 REQUIRED, 启动事务 T1
    public void callInnerMandatory_OuterRequired() {
        log.info("[OuterServiceMandatory.Scenario7] 开始执行 (外部 REQUIRED)...");
        logRepository.save(new LogEntry("OuterMandatory Log before calling inner"));
        log.info("[OuterServiceMandatory.Scenario7] 外部日志 (before) 已保存 (事务 T1).");

        try {
            // 调用 InnerServiceMandatory 的 mandatoryAction 方法
            // 因为当前方法有事务 T1，InnerServiceMandatory (MANDATORY) 会成功加入这个事务 T1
            innerServiceMandatory.mandatoryAction("Data from OuterMandatory (REQUIRED)");
            log.info("[OuterServiceMandatory.Scenario7] InnerServiceMandatory 调用成功.");
        } catch (RuntimeException e) {
            // 如果 innerServiceMandatory 内部发生其他运行时异常，会在这里捕获并导致 T1 回滚
            log.error("[OuterServiceMandatory.Scenario7] 捕获到来自 InnerServiceMandatory 的异常: {}", e.getMessage());
            throw e; // 重新抛出以确保 T1 回滚
        }

        logRepository.save(new LogEntry("OuterMandatory Log after calling inner"));
        log.info("[OuterServiceMandatory.Scenario7] 外部日志 (after) 已保存 (事务 T1).");
        log.info("[OuterServiceMandatory.Scenario7] 执行完毕 (外部 REQUIRED).");
        // T1 正常结束，提交
    }

    // === 场景 8: 外部无事务, 内部 MANDATORY ===
    // 这个方法 *没有* @Transactional 注解
    public void callInnerMandatory_OuterNonTransactional() {
        log.info("[OuterServiceMandatory.Scenario8] 开始执行 (外部无事务)...");
        try {
            // 调用 InnerServiceMandatory 的 mandatoryAction 方法
            // !! 关键：因为当前方法没有事务，InnerServiceMandatory (MANDATORY) 会立即抛出 IllegalTransactionStateException
            log.info("[OuterServiceMandatory.Scenario8] 即将调用 InnerServiceMandatory...");
            innerServiceMandatory.mandatoryAction("Data from Non-Transactional OuterMandatory");
            // 如果能执行到这里，说明 MANDATORY 的行为不符合预期
            log.warn("[OuterServiceMandatory.Scenario8] InnerServiceMandatory 调用似乎成功了？(预期应该抛异常)");
        } catch (IllegalTransactionStateException e) {
            // 捕获预期的异常
            log.error("[OuterServiceMandatory.Scenario8] 成功捕获到预期的异常 (因为外部无事务): {}", e.getMessage());
            // 在这里可以处理这个错误，比如返回特定信息给调用者
        } catch (RuntimeException e) {
            // 捕获其他可能的运行时异常
             log.error("[OuterServiceMandatory.Scenario8] 捕获到意外的运行时异常: {}", e.getMessage());
        }
        log.info("[OuterServiceMandatory.Scenario8] 执行完毕 (外部无事务).");
    }
}