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
public class OuterServiceNewTx {

    private final LogRepository logRepository;
    private final InnerServiceNewTx innerServiceNewTx; // 注入新的 Inner Service

    // === 场景 3: 外部 REQUIRED, 内部 REQUIRES_NEW ===
    @Transactional // 默认 REQUIRED, 启动事务 T1
    public void callInnerRequiresNew_OuterRequired(boolean innerShouldFail, boolean outerShouldFailAfter) {
        log.info("[OuterServiceNewTx.Scenario3] 开始执行 (外部 REQUIRED)...");
        logRepository.save(new LogEntry("OuterNewTx Log before calling inner"));
        log.info("[OuterServiceNewTx.Scenario3] 外部日志 (before) 已保存 (事务 T1).");

        try {
            // 调用 InnerServiceNewTx 的 requiresNewAction 方法
            // 当前事务 T1 会被挂起，innerServiceNewTx 会启动一个全新的独立事务 T2
            innerServiceNewTx.requiresNewAction("Data from OuterNewTx", innerShouldFail);
            // 当 requiresNewAction 执行完毕后，T2 会独立提交或回滚，然后 T1 恢复
            log.info("[OuterServiceNewTx.Scenario3] InnerServiceNewTx 调用完毕，外部事务 T1 已恢复.");
        } catch (RuntimeException e) {
            // 这个 catch 块只会捕获到 InnerServiceNewTx 抛出的异常
            // 但 T2 的回滚已经在其内部处理了
            log.error("[OuterServiceNewTx.Scenario3] 捕获到来自 InnerServiceNewTx 的异常: {}", e.getMessage());
            // 注意：即使捕获了内部异常，如果希望外部事务T1也因此失败，需要重新抛出
            // throw e; // 如果希望外部也失败，取消此行注释
        }

        // T1 恢复后继续执行
        if (outerShouldFailAfter) {
            log.warn("[OuterServiceNewTx.Scenario3] 外部事务 T1 即将抛出异常!");
            throw new RuntimeException("OuterServiceNewTx 在内部调用后失败了!"); // 这将导致 T1 回滚
        }

        logRepository.save(new LogEntry("OuterNewTx Log after calling inner"));
        log.info("[OuterServiceNewTx.Scenario3] 外部日志 (after) 已保存 (事务 T1).");
        log.info("[OuterServiceNewTx.Scenario3] 执行完毕 (外部 REQUIRED).");
        // 如果 T1 正常结束，它会在这里提交
    }


    // === 场景 4: 外部无事务, 内部 REQUIRES_NEW ===
    // 这个方法 *没有* @Transactional 注解
    public void callInnerRequiresNew_OuterNonTransactional(boolean innerShouldFail) {
        log.info("[OuterServiceNewTx.Scenario4] 开始执行 (外部无事务)...");
        try {
            // 调用 InnerServiceNewTx 的 requiresNewAction 方法
            // 因为当前没有外部事务，innerServiceNewTx 会简单地创建一个新事务 T2
            innerServiceNewTx.requiresNewAction("Data from Non-Transactional OuterNewTx", innerShouldFail);
            // 如果 innerServiceNewTx 成功，T2 在其执行完毕后提交
        } catch (RuntimeException e) {
            // 如果 innerServiceNewTx 失败，T2 会回滚
            log.error("[OuterServiceNewTx.Scenario4] 捕获到来自 InnerServiceNewTx 的异常: {}", e.getMessage());
        }
        log.info("[OuterServiceNewTx.Scenario4] 执行完毕 (外部无事务).");
    }
}