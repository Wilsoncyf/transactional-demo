package com.example.transactionaldemo.controller;

import com.example.transactionaldemo.service.OuterServiceNotSupported;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notsupported") // 新的前缀 /notsupported
@RequiredArgsConstructor
public class DemoNotSupportedController {

    private final OuterServiceNotSupported outerServiceNotSupported;

    // --- 场景 9: 外部 REQUIRED, 内部 NOT_SUPPORTED ---

    @GetMapping("/scenario9/inner_success_outer_success")
    public String runScenario9_InnerSuccess_OuterSuccess() {
        try {
            outerServiceNotSupported.callInnerNotSupported_OuterRequired(false, false);
            return "场景9 (内成功, 外成功): T1 提交, Inner 非事务性执行成功。预期 DB 有 Outer(before), Inner, Outer(after)。";
        } catch (Exception e) {
            return "场景9 (内成功, 外成功) 发生意外错误: " + e.getMessage();
        }
    }

    @GetMapping("/scenario9/inner_fail_outer_success")
    public String runScenario9_InnerFail_OuterSuccess() {
        // OuterService 捕获了内部异常并未重抛 (在 outerShouldFailAfter = false 时)
        outerServiceNotSupported.callInnerNotSupported_OuterRequired(true, false);
        return "场景9 (内失败, 外成功): T1 提交 (因异常被捕获), Inner 非事务性执行失败。预期 DB 有 Outer(before), Outer(after), 和失败前保存的 Inner 日志！";
    }

    @GetMapping("/scenario9/inner_success_outer_fail")
    public String runScenario9_InnerSuccess_OuterFail() {
        try {
            outerServiceNotSupported.callInnerNotSupported_OuterRequired(false, true);
            return "场景9 (内成功, 外失败) 执行完成，但似乎外部未回滚？"; // 不会到这里
        } catch (RuntimeException e) {
            return "场景9 (内成功, 外失败): T1 回滚, Inner 非事务性执行成功。预期 DB **只有** Inner 日志！异常: " + e.getMessage();
        }
    }

    // --- 场景 10: 外部无事务, 内部 NOT_SUPPORTED ---

    @GetMapping("/scenario10/inner_success")
    public String runScenario10_InnerSuccess() {
        outerServiceNotSupported.callInnerNotSupported_OuterNonTransactional(false);
        return "场景10 (内成功): Inner 非事务性执行。预期 DB 只有 Inner 日志。";
    }

    @GetMapping("/scenario10/inner_fail")
    public String runScenario10_InnerFail() {
        outerServiceNotSupported.callInnerNotSupported_OuterNonTransactional(true);
        return "场景10 (内失败): Inner 非事务性执行，失败前保存的日志无法回滚！预期 DB **仍然有** Inner 日志！";
    }
}