package com.ticketflow.ticket.domain;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ticketflow.ticket.entity.Ticket;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 工单数据范围条件。
 *
 * <p>该对象把“本人、本部门、本部门及下级、全部数据”转换为工单维度可复用的过滤条件。
 * 服务层可以把条件应用到 MyBatis-Plus 查询，也可以在单元测试或内存统计中直接判断工单是否命中范围。</p>
 *
 * @param allData 是否允许查看全部工单，允许时不会追加创建人或部门条件。
 * @param creatorId 限制创建人 ID，用于“本人”数据范围。
 * @param assigneeId 当前处理人 ID，用于保证处理人能访问被分派给自己的工单。
 * @param creatorDeptIds 限制创建部门 ID 集合，用于“本部门”和“本部门及下级”数据范围。
 */
public record TicketDataScopeCriteria(
        boolean allData,
        Long creatorId,
        Long assigneeId,
        List<Long> creatorDeptIds
) {

    private static final Long IMPOSSIBLE_DEPT_ID = Long.MIN_VALUE;

    /**
     * 创建全部数据范围条件。
     *
     * @return 不限制创建人和部门的数据范围条件。
     */
    public static TicketDataScopeCriteria all() {
        return new TicketDataScopeCriteria(true, null, null, List.of());
    }

    /**
     * 创建本人数据范围条件。
     *
     * @param creatorId 当前登录用户 ID。
     * @return 仅匹配本人创建工单的数据范围条件。
     */
    public static TicketDataScopeCriteria self(Long creatorId) {
        return new TicketDataScopeCriteria(false, creatorId, creatorId, List.of());
    }

    /**
     * 创建部门数据范围条件。
     *
     * @param creatorDeptIds 当前用户可查看的部门 ID 集合。
     * @return 仅匹配指定部门创建工单的数据范围条件。
     */
    public static TicketDataScopeCriteria depts(Collection<Long> creatorDeptIds) {
        return depts(creatorDeptIds, null);
    }

    /**
     * 创建部门数据范围条件，并允许当前处理人访问被指派给自己的工单。
     *
     * @param creatorDeptIds 当前用户可查看的部门 ID 集合。
     * @param assigneeId 当前登录用户 ID。
     * @return 匹配指定部门创建工单或指派给当前用户工单的数据范围条件。
     */
    public static TicketDataScopeCriteria depts(Collection<Long> creatorDeptIds, Long assigneeId) {
        List<Long> deptIds = creatorDeptIds == null
                ? List.of()
                : creatorDeptIds.stream().filter(Objects::nonNull).distinct().toList();
        return new TicketDataScopeCriteria(false, null, assigneeId, deptIds);
    }

    /**
     * 判断指定工单是否命中当前数据范围。
     *
     * @param ticket 待判断的工单。
     * @return 命中返回 true，否则返回 false。
     */
    public boolean matches(Ticket ticket) {
        if (ticket == null) {
            return false;
        }
        if (allData) {
            return true;
        }
        if (assigneeId != null && Objects.equals(ticket.getAssigneeId(), assigneeId)) {
            return true;
        }
        if (creatorId != null) {
            return Objects.equals(ticket.getCreatorId(), creatorId);
        }
        return creatorDeptIds.contains(ticket.getCreatorDeptId());
    }

    /**
     * 将数据范围条件追加到 MyBatis-Plus 查询包装器。
     *
     * @param query 工单查询包装器。
     */
    public void applyTo(LambdaQueryWrapper<Ticket> query) {
        if (allData) {
            return;
        }
        query.and(scope -> {
            boolean hasCondition = false;
            if (assigneeId != null) {
                scope.eq(Ticket::getAssigneeId, assigneeId);
                hasCondition = true;
            }
            if (creatorId != null) {
                if (hasCondition) {
                    scope.or();
                }
                scope.eq(Ticket::getCreatorId, creatorId);
                hasCondition = true;
            }
            if (!creatorDeptIds.isEmpty()) {
                if (hasCondition) {
                    scope.or();
                }
                scope.in(Ticket::getCreatorDeptId, creatorDeptIds);
                hasCondition = true;
            }
            if (!hasCondition) {
                scope.eq(Ticket::getCreatorDeptId, IMPOSSIBLE_DEPT_ID);
            }
        });
    }
}
