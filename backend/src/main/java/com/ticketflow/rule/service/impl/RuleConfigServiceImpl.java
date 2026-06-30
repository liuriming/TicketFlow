package com.ticketflow.rule.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketflow.common.cache.RedisJsonCacheService;
import com.ticketflow.common.cache.TicketFlowCacheKeys;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.rule.dto.DispatchRuleSaveRequest;
import com.ticketflow.rule.dto.SlaRuleSaveRequest;
import com.ticketflow.rule.dto.TicketCategorySaveRequest;
import com.ticketflow.rule.entity.DispatchRule;
import com.ticketflow.rule.mapper.DispatchRuleMapper;
import com.ticketflow.rule.service.RuleConfigService;
import com.ticketflow.sla.entity.SlaRule;
import com.ticketflow.sla.mapper.SlaRuleMapper;
import com.ticketflow.ticket.entity.TicketCategory;
import com.ticketflow.ticket.mapper.TicketCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Duration;

/**
 * 规则配置服务实现类。
 *
 * <p>负责工单分类、派单规则和 SLA 规则的基础维护。规则实际匹配逻辑由派单引擎和 SLA 计算器负责。</p>
 */
@Service
@RequiredArgsConstructor
public class RuleConfigServiceImpl implements RuleConfigService {

    private final TicketCategoryMapper categoryMapper;
    private final DispatchRuleMapper dispatchRuleMapper;
    private final SlaRuleMapper slaRuleMapper;
    private final RedisJsonCacheService cacheService;

    private static final Duration DICTIONARY_CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public List<TicketCategory> listCategories() {
        String cacheKey = TicketFlowCacheKeys.dictionary("ticket-category:list");
        return cacheService.get(cacheKey, new TypeReference<List<TicketCategory>>() {
        }).orElseGet(() -> {
            List<TicketCategory> categories = categoryMapper.selectList(Wrappers.<TicketCategory>lambdaQuery()
                .orderByAsc(TicketCategory::getSortOrder)
                .orderByAsc(TicketCategory::getId));
            cacheService.put(cacheKey, categories, DICTIONARY_CACHE_TTL);
            return categories;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketCategory saveCategory(Long id, TicketCategorySaveRequest request) {
        TicketCategory category = id == null ? new TicketCategory() : categoryMapper.selectById(id);
        if (category == null) {
            category = new TicketCategory();
        }
        category.setParentId(request.parentId() == null ? 0L : request.parentId());
        category.setCategoryName(request.categoryName());
        category.setCategoryCode(request.categoryCode());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setEnabled(1);
        if (category.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
        cacheService.delete(TicketFlowCacheKeys.dictionary("ticket-category:list"));
        cacheService.deleteByPattern(TicketFlowCacheKeys.hotStatsPattern());
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketCategory updateCategoryEnabled(Long id, Integer enabled) {
        TicketCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单分类不存在");
        }
        category.setEnabled(normalizeEnabled(enabled));
        categoryMapper.updateById(category);
        cacheService.delete(TicketFlowCacheKeys.dictionary("ticket-category:list"));
        cacheService.deleteByPattern(TicketFlowCacheKeys.hotStatsPattern());
        return category;
    }

    @Override
    public List<DispatchRule> listDispatchRules() {
        return dispatchRuleMapper.selectList(Wrappers.<DispatchRule>lambdaQuery()
                .orderByDesc(DispatchRule::getPriority)
                .orderByAsc(DispatchRule::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DispatchRule saveDispatchRule(Long id, DispatchRuleSaveRequest request) {
        DispatchRule rule = id == null ? new DispatchRule() : dispatchRuleMapper.selectById(id);
        if (rule == null) {
            rule = new DispatchRule();
        }
        rule.setCategoryId(request.categoryId());
        rule.setDeptId(request.deptId());
        rule.setSkillCode(request.skillCode());
        rule.setAssigneeId(request.assigneeId());
        rule.setPriority(request.priority() == null ? 0 : request.priority());
        rule.setEnabled(1);
        if (rule.getId() == null) {
            dispatchRuleMapper.insert(rule);
        } else {
            dispatchRuleMapper.updateById(rule);
        }
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DispatchRule updateDispatchRuleEnabled(Long id, Integer enabled) {
        DispatchRule rule = dispatchRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "派单规则不存在");
        }
        rule.setEnabled(normalizeEnabled(enabled));
        dispatchRuleMapper.updateById(rule);
        return rule;
    }

    @Override
    public List<SlaRule> listSlaRules() {
        String cacheKey = TicketFlowCacheKeys.dictionary("sla-rule:list");
        return cacheService.get(cacheKey, new TypeReference<List<SlaRule>>() {
        }).orElseGet(() -> {
            List<SlaRule> rules = slaRuleMapper.selectList(Wrappers.<SlaRule>lambdaQuery()
                    .orderByAsc(SlaRule::getPriority));
            cacheService.put(cacheKey, rules, DICTIONARY_CACHE_TTL);
            return rules;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SlaRule saveSlaRule(Long id, SlaRuleSaveRequest request) {
        SlaRule rule = id == null ? new SlaRule() : slaRuleMapper.selectById(id);
        if (rule == null) {
            rule = new SlaRule();
        }
        rule.setPriority(request.priority());
        rule.setResponseMinutes(request.responseMinutes());
        rule.setResolveMinutes(request.resolveMinutes());
        rule.setEnabled(1);
        if (rule.getId() == null) {
            slaRuleMapper.insert(rule);
        } else {
            slaRuleMapper.updateById(rule);
        }
        cacheService.delete(TicketFlowCacheKeys.dictionary("sla-rule:list"));
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SlaRule updateSlaRuleEnabled(Long id, Integer enabled) {
        SlaRule rule = slaRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "SLA 规则不存在");
        }
        rule.setEnabled(normalizeEnabled(enabled));
        slaRuleMapper.updateById(rule);
        cacheService.delete(TicketFlowCacheKeys.dictionary("sla-rule:list"));
        return rule;
    }

    private Integer normalizeEnabled(Integer enabled) {
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "启用状态只能是 0 或 1");
        }
        return enabled;
    }
}
