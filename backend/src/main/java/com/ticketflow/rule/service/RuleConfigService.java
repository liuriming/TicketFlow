package com.ticketflow.rule.service;

import com.ticketflow.rule.dto.DispatchRuleSaveRequest;
import com.ticketflow.rule.dto.SlaRuleSaveRequest;
import com.ticketflow.rule.dto.TicketCategorySaveRequest;
import com.ticketflow.rule.entity.DispatchRule;
import com.ticketflow.sla.entity.SlaRule;
import com.ticketflow.ticket.entity.TicketCategory;

import java.util.List;

/**
 * 规则配置服务接口。
 *
 * <p>统一封装工单分类、派单规则和 SLA 规则的配置维护能力。</p>
 */
public interface RuleConfigService {

    /**
     * 查询工单分类列表。
     *
     * @return 工单分类列表。
     */
    List<TicketCategory> listCategories();

    /**
     * 保存工单分类。
     *
     * @param id 分类 ID，传空表示新增。
     * @param request 分类保存请求。
     * @return 保存后的分类。
     */
    TicketCategory saveCategory(Long id, TicketCategorySaveRequest request);

    /**
     * 查询派单规则列表。
     *
     * @return 派单规则列表。
     */
    List<DispatchRule> listDispatchRules();

    /**
     * 保存派单规则。
     *
     * @param id 规则 ID，传空表示新增。
     * @param request 派单规则保存请求。
     * @return 保存后的派单规则。
     */
    DispatchRule saveDispatchRule(Long id, DispatchRuleSaveRequest request);

    /**
     * 查询 SLA 规则列表。
     *
     * @return SLA 规则列表。
     */
    List<SlaRule> listSlaRules();

    /**
     * 保存 SLA 规则。
     *
     * @param id 规则 ID，传空表示新增。
     * @param request SLA 规则保存请求。
     * @return 保存后的 SLA 规则。
     */
    SlaRule saveSlaRule(Long id, SlaRuleSaveRequest request);
}
