package com.example.transactionaldemo.service;

import com.example.transactionaldemo.service.OuterServiceSupports;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/supports") // 新的前缀 /supports
@RequiredArgsConstructor
public class DemoSupportsController {

    private final OuterServiceSupports outerServiceSupports;

    // --- 场景 5: 外部 REQUIRED, 内部 SUPPORTS ---

    @GetMapping("/scenario5/inner_success")
    public String runScenario5_InnerSuccess() {
        try {
            outerServiceSupports.callInnerSupports_OuterRequired(false);
            return "场景5 (内成功): Inner 加入 Outer 的事务 T1，T1 提交。预期 DB 有 Outer(before), Inner, Outer(after)。";
        } catch (Exception e) {
            return "场景5 (内成功) 发生意外错误: " + e.getMessage();
        }
    }

    @GetMapping("/scenario5/inner_fail")
    public String runScenario5_InnerFail() {
        try {
            outerServiceSupports.callInnerSupports_OuterRequired(true);
            return "场景5 (内失败) 执行完成，但似乎未回滚？"; // 正常不会到这里
        } catch (RuntimeException e) {
            return "场景5 (内失败): Inner 加入 T1 并失败，导致 T1 回滚。预期 DB 无新增记录。异常: " + e.getMessage();
        }
    }

    // --- 场景 6: 外部无事务, 内部 SUPPORTS ---

    @GetMapping("/scenario6/inner_success")
    public String runScenario6_InnerSuccess() {
        outerServiceSupports.callInnerSupports_OuterNonTransactional(false);
        return "场景6 (内成功): Inner 以非事务方式执行，日志可能已自动提交。预期 DB 只有 Inner 日志。";
    }

    @GetMapping("/scenario6/inner_fail")
    public String runScenario6_InnerFail() {
        outerServiceSupports.callInnerSupports_OuterNonTransactional(true);
        return "场景6 (内失败): Inner 以非事务方式执行，在失败前回滚了日志，但因无事务，该日志 *无法* 被回滚！预期 DB 仍然 *有* Inner 日志。";
    }
}