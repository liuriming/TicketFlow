package com.ticketflow.rule.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketflow.rule.domain.AutoDispatchPlanner;
import com.ticketflow.rule.domain.AutoDispatchResult;
import com.ticketflow.rule.domain.AutoDispatchRule;
import com.ticketflow.rule.domain.AutoDispatchTicket;
import com.ticketflow.rule.entity.DispatchRule;
import com.ticketflow.rule.mapper.DispatchRuleMapper;
import com.ticketflow.rule.service.AutoDispatchService;
import com.ticketflow.ticket.entity.Ticket;
import com.ticketflow.ticket.entity.TicketCategory;
import com.ticketflow.ticket.enums.TicketStatus;
import com.ticketflow.ticket.mapper.TicketCategoryMapper;
import com.ticketflow.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 自动派单服务实现类。
 *
 * <p>该实现从数据库读取启用的派单规则和工单分类，并统计候选处理人当前未关闭工单数量，
 * 再交由纯领域规划器计算最终处理人。这样既能使用数据库状态，又能保持派单算法可单元测试。</p>
 */
@Service
@RequiredArgsConstructor
public class AutoDispatchServiceImpl implements AutoDispatchService {

    private final TicketCategoryMapper categoryMapper;
    private final DispatchRuleMapper dispatchRuleMapper;
    private final TicketMapper ticketMapper;

    @Override
    public AutoDispatchResult dispatch(Ticket ticket) {
        TicketCategory category = ticket.getCategoryId() == null ? null : categoryMapper.selectById(ticket.getCategoryId());
        String categoryCode = category == null ? null : category.getCategoryCode();
        List<AutoDispatchRule> rules = dispatchRuleMapper.selectList(Wrappers.<DispatchRule>lambdaQuery()
                        .eq(DispatchRule::getEnabled, 1))
                .stream()
                .map(this::toSnapshot)
                .toList();
        Map<Long, Integer> loadByAssignee = rules.stream()
                .map(AutoDispatchRule::assigneeId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(assigneeId -> assigneeId, this::countActiveTickets));
        return AutoDispatchPlanner.plan(
                new AutoDispatchTicket(ticket.getId(), ticket.getCategoryId(), ticket.getCreatorDeptId(), categoryCode),
                rules,
                loadByAssignee
        );
    }

    private AutoDispatchRule toSnapshot(DispatchRule rule) {
        return new AutoDispatchRule(
                rule.getId(),
                rule.getCategoryId(),
                rule.getDeptId(),
                rule.getSkillCode(),
                rule.getAssigneeId(),
                rule.getPriority()
        );
    }

    private Integer countActiveTickets(Long assigneeId) {
        Long count = ticketMapper.selectCount(Wrappers.<Ticket>lambdaQuery()
                .eq(Ticket::getAssigneeId, assigneeId)
                .notIn(Ticket::getStatus, TicketStatus.CLOSED, TicketStatus.CANCELED));
        return count.intValue();
    }
}
