package com.example.transactionaldemo.controller;

import com.example.transactionaldemo.service.OuterServiceNested;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nested") // New prefix /nested
@RequiredArgsConstructor
public class DemoNestedController {

    private final OuterServiceNested outerServiceNested;

    // --- Scenario 13: Inner NESTED Fails, Outer REQUIRED Succeeds ---
    @GetMapping("/scenario13/inner_fail_outer_success")
    public String runScenario13_InnerFailOuterSuccess() {
        try {
            outerServiceNested.scenarioInnerFailOuterSuccess();
            // Outer service caught the inner exception and continued
            return "Scenario 13 (Inner Fail, Outer Success): Inner rolled back to savepoint, Outer committed. Check DB & Logs. Expected DB: Outer Log 1, Outer Log 2.";
        } catch (Exception e) {
            // This shouldn't happen if OuterService catches correctly
            return "Scenario 13 occurred an unexpected error: " + e.getMessage();
        }
    }

    // --- Scenario 14: Inner NESTED Succeeds, Outer REQUIRED Fails ---
    @GetMapping("/scenario14/inner_success_outer_fail")
    public String runScenario14_InnerSuccessOuterFail() {
        try {
            outerServiceNested.scenarioInnerSuccessOuterFail();
            // This shouldn't be reached as OuterService throws an exception
            return "Scenario 14 executed but outer didn't seem to fail?";
        } catch (RuntimeException e) {
            return "Scenario 14 (Inner Success, Outer Fail): Inner succeeded, but Outer failed later, causing full T1 rollback. Expected DB: No new records. Exception: " + e.getMessage();
        }
    }
}