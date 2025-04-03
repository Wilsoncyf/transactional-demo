package com.example.transactionaldemo.service;

import com.example.transactionaldemo.entity.LogEntry;
import com.example.transactionaldemo.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport; // 用于手动回滚

@Service
@Slf4j
@RequiredArgsConstructor
public class OuterService {

    private final LogRepository logRepository;
    private final InnerService innerService;

    // === 场景 1: REQUIRED 加入现有事务 ===
    @Transactional // 默认 REQUIRED
    public void callInnerRequired(boolean innerShouldFail) {
        log.info("[OuterService.callInnerRequired] 开始执行...");
        logRepository.save(new LogEntry("Outer Log before calling inner"));
        log.info("[OuterService.callInnerRequired] 外部日志 (before) 已保存.");

        try {
            // 调用 InnerService 的 requiredAction 方法
            // 因为当前方法有事务(T1)，InnerService 的方法会加入这个事务(T1)
            innerService.requiredAction("Data from Outer", innerShouldFail);
        } catch (RuntimeException e) {
            log.error("[OuterService.callInnerRequired] 捕获到来自 InnerService 的异常: {}", e.getMessage());
            // 关键：为了让整个事务 T1 回滚，必须重新抛出异常或者手动设置回滚
            // 否则，这里的 catch 会“吞掉”异常，导致 T1 错误地提交！
            // 选择 1: 重新抛出 (更常见)
             throw e;
            // 选择 2: 手动标记回滚 (如果不想让异常继续向上传播)
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // log.info("[OuterService.callInnerRequired] 已手动标记事务为 rollback-only");
        }

        // 这段代码只有在 innerService.requiredAction 没有抛异常，
        // 或者抛了异常但被上面 catch 住且没有重新抛出/手动回滚时才会执行
        logRepository.save(new LogEntry("Outer Log after calling inner"));
        log.info("[OuterService.callInnerRequired] 外部日志 (after) 已保存.");
        log.info("[OuterService.callInnerRequired] 执行完毕.");
    }

    // === 场景 2: REQUIRED 创建新事务 ===
    // 这个方法 *没有* @Transactional 注解
    public void callInnerFromNonTransactional(boolean innerShouldFail) {
        log.info("[OuterService.callInnerFromNonTransactional] 开始执行 (无事务)...");
        try {
            // 调用 InnerService 的 requiredAction 方法
            // 因为当前方法没有事务，InnerService 的方法会自己创建一个新事务(T2)
            innerService.requiredAction("Data from Non-Transactional Outer", innerShouldFail);
            // 如果 innerService 成功，T2 在这里执行完后会提交
        } catch (RuntimeException e) {
            // 如果 innerService 失败，T2 会回滚
            // 这个 catch 只会捕获异常，不会影响其他任何事务（因为外部没有事务）
            log.error("[OuterService.callInnerFromNonTransactional] 捕获到来自 InnerService 的异常: {}", e.getMessage());
        }
        // 无论 innerService 成功或失败，这里的代码都会继续执行
        log.info("[OuterService.callInnerFromNonTransactional] 执行完毕 (无事务).");
    }
}