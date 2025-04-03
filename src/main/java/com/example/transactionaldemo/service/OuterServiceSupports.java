package com.example.transactionaldemo.service;

import com.example.transactionaldemo.entity.LogEntry;
import com.example.transactionaldemo.repository.LogRepository;
import com.example.transactionaldemo.service.InnerServiceSupports; // 注入 Supports Service
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OuterServiceSupports {

    private final LogRepository logRepository;
    private final InnerServiceSupports innerServiceSupports; // 注入 Supports Service

    // === 场景 5: 外部 REQUIRED, 内部 SUPPORTS ===
    @Transactional // 默认 REQUIRED, 启动事务 T1
    public void callInnerSupports_OuterRequired(boolean innerShouldFail) {
        log.info("[OuterServiceSupports.Scenario5] 开始执行 (外部 REQUIRED)...");
        logRepository.save(new LogEntry("OuterSupports Log before calling inner"));
        log.info("[OuterServiceSupports.Scenario5] 外部日志 (before) 已保存 (事务 T1).");

        try {
            // 调用 InnerServiceSupports 的 supportsAction 方法
            // 因为当前方法有事务 T1，InnerServiceSupports 的方法会加入这个事务 T1
            innerServiceSupports.supportsAction("Data from OuterSupports", innerShouldFail);
        } catch (RuntimeException e) {
            log.error("[OuterServiceSupports.Scenario5] 捕获到来自 InnerServiceSupports 的异常: {}", e.getMessage());
            // 因为 inner 加入了 T1，它的失败会导致 T1 回滚，所以这里需要重新抛出异常
            throw e;
        }

        // 这段代码只有在 innerServiceSupports.supportsAction 没有抛异常时才会执行
        logRepository.save(new LogEntry("OuterSupports Log after calling inner"));
        log.info("[OuterServiceSupports.Scenario5] 外部日志 (after) 已保存 (事务 T1).");
        log.info("[OuterServiceSupports.Scenario5] 执行完毕 (外部 REQUIRED).");
        // 如果 T1 正常结束，它会在这里提交
    }

    // === 场景 6: 外部无事务, 内部 SUPPORTS ===
    // 这个方法 *没有* @Transactional 注解
    public void callInnerSupports_OuterNonTransactional(boolean innerShouldFail) {
        log.info("[OuterServiceSupports.Scenario6] 开始执行 (外部无事务)...");
        try {
            // 调用 InnerServiceSupports 的 supportsAction 方法
            // 因为当前方法没有事务，InnerServiceSupports 的方法会以非事务方式执行
            innerServiceSupports.supportsAction("Data from Non-Transactional OuterSupports", innerShouldFail);
        } catch (RuntimeException e) {
            // 这个 catch 只会捕获异常，因为没有事务，所以没有事务回滚发生
            log.error("[OuterServiceSupports.Scenario6] 捕获到来自 InnerServiceSupports 的异常: {}", e.getMessage());
            log.warn("[OuterServiceSupports.Scenario6] 注意：如果内部方法在抛异常前保存了数据，该数据可能已提交且无法回滚！");
        }
        log.info("[OuterServiceSupports.Scenario6] 执行完毕 (外部无事务).");
    }
}