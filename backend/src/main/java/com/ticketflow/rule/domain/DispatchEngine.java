package com.ticketflow.rule.domain;

import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 自动派单领域服务。
 *
 * <p>根据分类技能匹配、负责部门和当前负载选择最合适的运维人员。
 * 该类是纯领域逻辑，不直接访问数据库，便于单元测试和后续替换为更复杂的评分模型。</p>
 */
public final class DispatchEngine {

    private DispatchEngine() {
    }

    /**
     * 从候选人中选择最合适的处理人。
     *
     * @param context 派单上下文。
     * @param candidates 候选运维人员。
     * @return 最佳候选人。
     */
    public static Optional<DispatchCandidate> selectBest(DispatchContext context, List<DispatchCandidate> candidates) {
        if (context == null || CollectionUtils.isEmpty(candidates)) {
            return Optional.empty();
        }
        return candidates.stream()
                .filter(candidate -> matchesSkill(context, candidate))
                .max(Comparator.comparingInt(candidate -> score(context, candidate)));
    }

    private static boolean matchesSkill(DispatchContext context, DispatchCandidate candidate) {
        return candidate.skills() != null && candidate.skills().contains(context.categoryCode());
    }

    private static int score(DispatchContext context, DispatchCandidate candidate) {
        int score = 0;
        if (candidate.deptId() != null && candidate.deptId().equals(context.deptId())) {
            score += 30;
        }
        if (matchesSkill(context, candidate)) {
            score += 50;
        }
        score -= Math.max(candidate.currentLoad(), 0) * 5;
        return score;
    }
}
