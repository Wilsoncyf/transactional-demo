package com.example.transactionaldemo.controller;

import com.example.transactionaldemo.service.OuterServiceNewTx;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/newtx") // 给这个 Controller 加个前缀区分
@RequiredArgsConstructor
public class DemoNewTxController {

    private final OuterServiceNewTx outerServiceNewTx;

    // --- 场景 3: 外部 REQUIRED, 内部 REQUIRES_NEW ---

    @GetMapping("/scenario3/inner_success_outer_success")
    public String runScenario3_InnerSuccess_OuterSuccess() {
        try {
            outerServiceNewTx.callInnerRequiresNew_OuterRequired(false, false);
            return "场景3 (内成功, 外成功): T1 和 T2 都独立提交。预期数据库有 Outer(before), Inner, Outer(after) 日志。";
        } catch (Exception e) {
            return "场景3 (内成功, 外成功) 发生意外错误: " + e.getMessage();
        }
    }

    @GetMapping("/scenario3/inner_fail_outer_success")
    public String runScenario3_InnerFail_OuterSuccess() {
         // 注意：在 OuterServiceNewTx 中，我们没有重新抛出内部异常
         // 所以外部事务 T1 不会因此失败
        try {
             outerServiceNewTx.callInnerRequiresNew_OuterRequired(true, false);
             return "场景3 (内失败, 外成功): T2 回滚, T1 提交。预期数据库只有 Outer(before) 和 Outer(after) 日志。";
        } catch (Exception e) {
             // 如果 OuterServiceNewTx 修改为重新抛出内部异常，则会进入这里
             return "场景3 (内失败, 外成功) 发生意外错误（可能外部事务也回滚了）: " + e.getMessage();
        }
    }

    @GetMapping("/scenario3/inner_success_outer_fail")
    public String runScenario3_InnerSuccess_OuterFail() {
        try {
            outerServiceNewTx.callInnerRequiresNew_OuterRequired(false, true);
             // 因为外部 T1 失败并抛异常，会进入下面的 catch
            return "场景3 (内成功, 外失败) 执行完成，但似乎外部未回滚？";
        } catch (RuntimeException e) {
            return "场景3 (内成功, 外失败): T2 已提交, T1 回滚。预期数据库只有 Inner 日志。异常: " + e.getMessage();
        }
    }

    // --- 场景 4: 外部无事务, 内部 REQUIRES_NEW ---

    @GetMapping("/scenario4/inner_success")
    public String runScenario4_InnerSuccess() {
        outerServiceNewTx.callInnerRequiresNew_OuterNonTransactional(false);
        return "场景4 (内成功): T2 提交。预期数据库只有 Inner 日志。";
    }

    @GetMapping("/scenario4/inner_fail")
    public String runScenario4_InnerFail() {
        outerServiceNewTx.callInnerRequiresNew_OuterNonTransactional(true);
        return "场景4 (内失败): T2 回滚。预期数据库无新增日志。";
    }
}