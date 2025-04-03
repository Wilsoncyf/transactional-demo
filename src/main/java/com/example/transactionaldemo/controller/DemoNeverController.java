package com.example.transactionaldemo.controller;

import com.example.transactionaldemo.service.OuterServiceNever;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/never") // 新的前缀 /never
@RequiredArgsConstructor
public class DemoNeverController {

    private final OuterServiceNever outerServiceNever;

    // --- 场景 11: 外部 REQUIRED, 内部 NEVER ---
    @GetMapping("/scenario11/fail")
    public String runScenario11_Fail() {
        try {
             outerServiceNever.callInnerNever_OuterRequired();
             // 因为 Outer Service 捕获了异常，所以请求能正常返回
             return "场景11: Outer(REQUIRED) 调用 Inner(NEVER)，成功捕获 IllegalTransactionStateException。外部事务 T1 继续并提交。预期 DB 有 Outer(before), Outer(after)。请检查应用日志确认异常信息。";
        } catch (Exception e) {
             // 如果 OuterService 重抛了异常，会进入这里
             return "场景11 发生意外错误（可能是外部事务最终回滚了）: " + e.getMessage();
        }
    }

    // --- 场景 12: 外部无事务, 内部 NEVER ---
    @GetMapping("/scenario12/success")
    public String runScenario12_Success() {
        outerServiceNever.callInnerNever_OuterNonTransactional();
        return "场景12: Outer(Non-Tx) 调用 Inner(NEVER)，Inner 成功以非事务方式执行。预期 DB 有 Inner 日志。";
    }
}