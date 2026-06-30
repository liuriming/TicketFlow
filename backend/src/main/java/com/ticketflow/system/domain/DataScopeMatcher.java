package com.ticketflow.system.domain;

import com.ticketflow.system.enums.DataScopeType;
import org.springframework.util.StringUtils;

/**
 * 数据权限匹配器。
 *
 * <p>该类只处理“当前用户是否能访问目标数据”的纯领域判断，不依赖数据库和 Web 上下文，
 * 方便在工单查询、报表统计和单元测试中复用。</p>
 */
public final class DataScopeMatcher {

    private DataScopeMatcher() {
    }

    /**
     * 判断当前用户是否可以访问目标数据。
     *
     * @param scope 当前用户的数据权限范围。
     * @param currentUserId 当前用户 ID。
     * @param targetCreatorId 目标数据创建人 ID。
     * @param currentDeptPath 当前用户部门路径。
     * @param targetDeptPath 目标数据所属部门路径。
     * @return true 表示允许访问，false 表示拒绝访问。
     */
    public static boolean canAccess(
            DataScopeType scope,
            Long currentUserId,
            Long targetCreatorId,
            String currentDeptPath,
            String targetDeptPath
    ) {
        if (scope == null) {
            return false;
        }
        return switch (scope) {
            case ALL -> true;
            case SELF -> currentUserId != null && currentUserId.equals(targetCreatorId);
            case DEPT -> hasText(currentDeptPath) && currentDeptPath.equals(targetDeptPath);
            case DEPT_AND_CHILD -> isSameOrChildDept(currentDeptPath, targetDeptPath);
        };
    }

    private static boolean isSameOrChildDept(String currentDeptPath, String targetDeptPath) {
        if (!hasText(currentDeptPath) || !hasText(targetDeptPath)) {
            return false;
        }
        return targetDeptPath.equals(currentDeptPath) || targetDeptPath.startsWith(currentDeptPath + "/");
    }

    private static boolean hasText(String value) {
        return StringUtils.hasText(value);
    }
}
