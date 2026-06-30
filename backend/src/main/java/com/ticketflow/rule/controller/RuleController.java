package com.ticketflow.rule.controller;

import com.ticketflow.common.web.ApiResult;
import com.ticketflow.common.web.StatusUpdateRequest;
import com.ticketflow.rule.dto.DispatchRuleSaveRequest;
import com.ticketflow.rule.dto.SlaRuleSaveRequest;
import com.ticketflow.rule.dto.TicketCategorySaveRequest;
import com.ticketflow.rule.entity.DispatchRule;
import com.ticketflow.rule.service.RuleConfigService;
import com.ticketflow.sla.entity.SlaRule;
import com.ticketflow.system.annotation.RequirePermission;
import com.ticketflow.ticket.entity.TicketCategory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 规则接口。
 *
 * <p>用于维护工单分类、派单规则和 SLA 规则。</p>
 */
@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleConfigService ruleConfigService;

    @GetMapping("/categories")
    @RequirePermission("ticket:category:list")
    public ApiResult<List<TicketCategory>> categories() {
        return ApiResult.success(ruleConfigService.listCategories());
    }

    @PostMapping("/categories")
    @RequirePermission("ticket:category:write")
    public ApiResult<TicketCategory> createCategory(@Valid @RequestBody TicketCategorySaveRequest request) {
        return ApiResult.success(ruleConfigService.saveCategory(null, request));
    }

    @PutMapping("/categories/{id}")
    @RequirePermission("ticket:category:write")
    public ApiResult<TicketCategory> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody TicketCategorySaveRequest request
    ) {
        return ApiResult.success(ruleConfigService.saveCategory(id, request));
    }

    @PutMapping("/categories/{id}/enabled")
    @RequirePermission("ticket:category:write")
    public ApiResult<TicketCategory> updateCategoryEnabled(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return ApiResult.success(ruleConfigService.updateCategoryEnabled(id, request.enabled()));
    }

    @GetMapping("/dispatch")
    @RequirePermission("rule:dispatch:list")
    public ApiResult<List<DispatchRule>> dispatchRules() {
        return ApiResult.success(ruleConfigService.listDispatchRules());
    }

    @PostMapping("/dispatch")
    @RequirePermission("rule:dispatch:write")
    public ApiResult<DispatchRule> createDispatchRule(@RequestBody DispatchRuleSaveRequest request) {
        return ApiResult.success(ruleConfigService.saveDispatchRule(null, request));
    }

    @PutMapping("/dispatch/{id}")
    @RequirePermission("rule:dispatch:write")
    public ApiResult<DispatchRule> updateDispatchRule(
            @PathVariable Long id,
            @RequestBody DispatchRuleSaveRequest request
    ) {
        return ApiResult.success(ruleConfigService.saveDispatchRule(id, request));
    }

    @PutMapping("/dispatch/{id}/enabled")
    @RequirePermission("rule:dispatch:write")
    public ApiResult<DispatchRule> updateDispatchRuleEnabled(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return ApiResult.success(ruleConfigService.updateDispatchRuleEnabled(id, request.enabled()));
    }

    @GetMapping("/sla")
    @RequirePermission("rule:sla:list")
    public ApiResult<List<SlaRule>> slaRules() {
        return ApiResult.success(ruleConfigService.listSlaRules());
    }

    @PostMapping("/sla")
    @RequirePermission("rule:sla:write")
    public ApiResult<SlaRule> createSlaRule(@Valid @RequestBody SlaRuleSaveRequest request) {
        return ApiResult.success(ruleConfigService.saveSlaRule(null, request));
    }

    @PutMapping("/sla/{id}")
    @RequirePermission("rule:sla:write")
    public ApiResult<SlaRule> updateSlaRule(
            @PathVariable Long id,
            @Valid @RequestBody SlaRuleSaveRequest request
    ) {
        return ApiResult.success(ruleConfigService.saveSlaRule(id, request));
    }

    @PutMapping("/sla/{id}/enabled")
    @RequirePermission("rule:sla:write")
    public ApiResult<SlaRule> updateSlaRuleEnabled(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return ApiResult.success(ruleConfigService.updateSlaRuleEnabled(id, request.enabled()));
    }
}
