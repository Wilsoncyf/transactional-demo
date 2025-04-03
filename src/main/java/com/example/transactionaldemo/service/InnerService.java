package com.example.transactionaldemo.service;

import com.example.transactionaldemo.entity.LogEntry;
import com.example.transactionaldemo.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 使用 SLF4j 进行日志记录
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j // Lombok: 自动注入一个 log 实例
@RequiredArgsConstructor // Lombok: 为 final 字段生成构造函数，用于依赖注入
public class InnerService {

    // 使用 final 和 @RequiredArgsConstructor 是推荐的注入方式之一
    private final LogRepository logRepository;

    // @Transactional 默认就是 Propagation.REQUIRED
    @Transactional // 等同于 @Transactional(propagation = Propagation.REQUIRED)
    public void requiredAction(String message, boolean shouldFail) {
        log.info("  [InnerService.requiredAction] 开始执行...");
        logRepository.save(new LogEntry("Inner Log: " + message));
        log.info("  [InnerService.requiredAction] 内部日志已保存.");

        if (shouldFail) {
            log.warn("  [InnerService.requiredAction] 即将抛出运行时异常!");
            throw new RuntimeException("InnerService 失败了!");
        }
        log.info("  [InnerService.requiredAction] 执行完毕.");
    }
}