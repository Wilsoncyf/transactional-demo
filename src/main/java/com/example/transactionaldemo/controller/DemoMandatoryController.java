package com.example.transactionaldemo.controller;

import com.example.transactionaldemo.service.OuterServiceMandatory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mandatory") // 新的前缀 /mandatory
@RequiredArgsConstructor
public class DemoMandatoryController {

    private final OuterServiceMandatory outerServiceMandatory;

    // --- 场景 7: 外部 REQUIRED, 内部 MANDATORY ---
    @GetMapping("/scenario7/success")
    public String runScenario7_Success() {
        try {
            outerServiceMandatory.callInnerMandatory_OuterRequired();
            return "场景7: Inner(MANDATORY) 成功加入 Outer(REQUIRED) 的事务 T1，T1 提交。预期 DB 有 Outer(before), Inner, Outer(after)。";
        } catch (Exception e) {
            return "场景7 发生意外错误: " + e.getMessage();
        }
    }

    // --- 场景 8: 外部无事务, 内部 MANDATORY ---
    @GetMapping("/scenario8/fail")
    public String runScenario8_Fail() {
        // 这个方法会调用一个没有事务的 Outer 方法，
        // 该 Outer 方法内部调用 MANDATORY 的 Inner 方法，预期会抛出异常
        outerServiceMandatory.callInnerMandatory_OuterNonTransactional();
        // OuterService 内部应该已经捕获并记录了异常
        return "场景8: Outer(Non-Tx) 调用 Inner(MANDATORY)，成功捕获 IllegalTransactionStateException。预期 DB 无新增记录。请检查应用日志确认异常信息。";
    }
}