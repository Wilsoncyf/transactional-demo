package com.example.transactionaldemo.controller;

import com.example.transactionaldemo.service.OuterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DemoController {

    private final OuterService outerService;

    @GetMapping("/scenario1/success")
    public String runScenario1Success() {
        try {
            outerService.callInnerRequired(false); // 内部方法成功
            return "场景1执行成功 (内部成功)，检查日志和数据库（预期有3条记录）。";
        } catch (Exception e) {
            return "场景1执行时发生意外错误: " + e.getMessage();
        }
    }

    @GetMapping("/scenario1/fail")
    public String runScenario1Fail() {
        try {
            outerService.callInnerRequired(true); // 内部方法失败
            // 如果 OuterService 正确地重新抛出了异常，这里不会被执行
            return "场景1执行完成，但似乎内部异常未导致外部回滚？（检查OuterService的catch块）";
        } catch (RuntimeException e) {
            return "场景1执行失败 (内部失败)，触发回滚，检查日志和数据库（预期无记录）。异常: " + e.getMessage();
        }
    }

    @GetMapping("/scenario2/success")
    public String runScenario2Success() {
        outerService.callInnerFromNonTransactional(false); // 内部方法成功
        return "场景2执行成功 (内部成功)，检查日志和数据库（预期只有内部记录）。";
    }

    @GetMapping("/scenario2/fail")
    public String runScenario2Fail() {
        outerService.callInnerFromNonTransactional(true); // 内部方法失败
        return "场景2执行完成 (内部失败)，内部事务已回滚，检查日志和数据库（预期无记录）。";
    }
}