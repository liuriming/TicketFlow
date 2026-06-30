package com.ticketflow.rule.domain;

import com.ticketflow.ticket.enums.TicketStatus;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 自动派单规划器。
 *
 * <p>根据工单分类、部门、规则候选处理人和当前负载计算派单结果。
 * 该类不访问数据库，数据库查询由应用服务负责，便于保持派单规则可测试。</p>
 */
public final class AutoDispatchPlanner {

    private AutoDispatchPlanner() {
    }

    /**
     * 计算自动派单结果。
     *
     * @param ticket 待派单工单快照。
     * @param rules 启用的派单规则。
     * @param loadByAssignee 处理人当前负载，key 为用户 ID，value 为未关闭工单数。
     * @return 自动派单结果。
     */
    public static AutoDispatchResult plan(
            AutoDispatchTicket ticket,
            List<AutoDispatchRule> rules,
            Map<Long, Integer> loadByAssignee
    ) {
        if (ticket == null || CollectionUtils.isEmpty(rules)) {
            return pendingAssign("未匹配到派单规则");
        }
        List<AutoDispatchRule> matchedRules = rules.stream()
                .filter(rule -> rule.assigneeId() != null)
                .filter(rule -> matchesCategory(ticket, rule))
                .filter(rule -> matchesDept(ticket, rule))
                .filter(rule -> matchesSkill(ticket, rule))
                .toList();
        if (matchedRules.isEmpty()) {
            return pendingAssign("未匹配到派单规则");
        }
        AutoDispatchRule selected = matchedRules.stream()
                .max(Comparator.comparingInt((AutoDispatchRule rule) -> score(ticket, rule, loadByAssignee))
                        .thenComparing(rule -> rule.ruleId() == null ? 0L : -rule.ruleId()))
                .orElseThrow();
        return new AutoDispatchResult(
                true,
                selected.assigneeId(),
                TicketStatus.PENDING_ACCEPT,
                "自动派单给用户 " + selected.assigneeId()
        );
    }

    private static AutoDispatchResult pendingAssign(String reason) {
        return new AutoDispatchResult(false, null, TicketStatus.PENDING_ASSIGN, reason);
    }

    private static boolean matchesCategory(AutoDispatchTicket ticket, AutoDispatchRule rule) {
        return rule.categoryId() == null || Objects.equals(rule.categoryId(), ticket.categoryId());
    }

    private static boolean matchesDept(AutoDispatchTicket ticket, AutoDispatchRule rule) {
        return rule.deptId() == null || Objects.equals(rule.deptId(), ticket.deptId());
    }

    private static boolean matchesSkill(AutoDispatchTicket ticket, AutoDispatchRule rule) {
        return rule.skillCode() == null || rule.skillCode().isBlank() || rule.skillCode().equals(ticket.categoryCode());
    }

    private static int score(AutoDispatchTicket ticket, AutoDispatchRule rule, Map<Long, Integer> loadByAssignee) {
        int score = 0;
        score += rule.priority() == null ? 0 : rule.priority() * 10;
        if (Objects.equals(rule.categoryId(), ticket.categoryId())) {
            score += 40;
        }
        if (Objects.equals(rule.deptId(), ticket.deptId())) {
            score += 20;
        }
        if (Objects.equals(rule.skillCode(), ticket.categoryCode())) {
            score += 30;
        }
        int load = loadByAssignee == null ? 0 : loadByAssignee.getOrDefault(rule.assigneeId(), 0);
        score -= Math.max(load, 0) * 5;
        return score;
    }
}
