package com.example.transactionaldemo.service;

import com.example.transactionaldemo.entity.LogEntry;
import com.example.transactionaldemo.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OuterServiceNotSupported {

    private final LogRepository logRepository;
    private final InnerServiceNotSupported innerServiceNotSupported; // 注入 NotSupported Service

    // === 场景 9: 外部 REQUIRED, 内部 NOT_SUPPORTED ===
    @Transactional // 默认 REQUIRED, 启动事务 T1
    public void callInnerNotSupported_OuterRequired(boolean innerShouldFail, boolean outerShouldFailAfter) {
        log.info("[OuterServiceNotSupported.Scenario9] 开始执行 (外部 REQUIRED)...");
        logRepository.save(new LogEntry("OuterNotSupported Log before calling inner"));
        log.info("[OuterServiceNotSupported.Scenario9] 外部日志 (before) 已保存 (事务 T1).");

        String innerResult = "[Inner Call Result Unknown]";
        try {
            // 调用 InnerServiceNotSupported 的 notSupportedAction 方法
            // 当前事务 T1 会被挂起，innerServiceNotSupported 会以非事务方式执行
            innerServiceNotSupported.notSupportedAction("Data from OuterNotSupported", innerShouldFail);
            // 内部方法执行的 save 操作很可能已经提交了
            innerResult = "[Inner Call Succeeded]";
            log.info("[OuterServiceNotSupported.Scenario9] InnerServiceNotSupported 调用完毕，外部事务 T1 已恢复.");
        } catch (RuntimeException e) {
            innerResult = "[Inner Call Failed with Exception]";
            // 捕获内部抛出的异常，但 T1 仍然恢复
            log.error("[OuterServiceNotSupported.Scenario9] 捕获到来自 InnerServiceNotSupported 的异常: {}", e.getMessage());
            // 我们可以选择在这里让 T1 继续（不重抛），或者让 T1 也失败（重抛）
             if (!outerShouldFailAfter) { // 只有在外部不打算失败时，才可能考虑不重抛，但通常重抛更安全
                 log.warn("[OuterServiceNotSupported.Scenario9] 内部调用失败，但外部事务 T1 将继续尝试提交...");
             } else {
                 throw e; // 如果外部也要失败，或者为了保险，重新抛出
             }
        }

        // T1 恢复后继续执行
        log.info("[OuterServiceNotSupported.Scenario9] Inner call result: {}", innerResult);
        if (outerShouldFailAfter) {
            log.warn("[OuterServiceNotSupported.Scenario9] 外部事务 T1 即将抛出异常!");
            throw new RuntimeException("OuterServiceNotSupported 在内部调用后失败了!"); // 这将导致 T1 回滚
        }

        logRepository.save(new LogEntry("OuterNotSupported Log after calling inner"));
        log.info("[OuterServiceNotSupported.Scenario9] 外部日志 (after) 已保存 (事务 T1).");
        log.info("[OuterServiceNotSupported.Scenario9] 执行完毕 (外部 REQUIRED).");
        // 如果 T1 正常结束，它会在这里提交
    }

    // === 场景 10: 外部无事务, 内部 NOT_SUPPORTED ===
    // 这个方法 *没有* @Transactional 注解
    public void callInnerNotSupported_OuterNonTransactional(boolean innerShouldFail) {
        log.info("[OuterServiceNotSupported.Scenario10] 开始执行 (外部无事务)...");
        try {
            // 调用 InnerServiceNotSupported 的 notSupportedAction 方法
            // 因为当前没有外部事务，innerServiceNotSupported 会直接以非事务方式执行
            innerServiceNotSupported.notSupportedAction("Data from Non-Transactional OuterNotSupported", innerShouldFail);
        } catch (RuntimeException e) {
            log.error("[OuterServiceNotSupported.Scenario10] 捕获到来自 InnerServiceNotSupported 的异常: {}", e.getMessage());
             log.warn("[OuterServiceNotSupported.Scenario10] 注意：如果内部方法在抛异常前保存了数据，该数据可能已提交且无法回滚！");
        }
        log.info("[OuterServiceNotSupported.Scenario10] 执行完毕 (外部无事务).");
    }
}