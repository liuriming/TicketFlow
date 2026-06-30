package com.ticketflow.ticket.domain;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketflow.common.context.LoginUser;
import com.ticketflow.system.entity.SysDept;
import com.ticketflow.system.enums.DataScopeType;
import com.ticketflow.system.mapper.SysDeptMapper;
import com.ticketflow.ticket.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 工单数据范围支持组件。
 *
 * <p>该组件集中解析当前登录用户的数据权限，避免工单列表、统计报表等模块各自拼装查询条件。
 * 对“本部门及下级”范围，会基于部门 path 查询当前部门和所有下级部门 ID。</p>
 */
@Component
@RequiredArgsConstructor
public class TicketDataScopeSupport {

    private final SysDeptMapper deptMapper;

    /**
     * 根据登录用户解析工单数据范围条件。
     *
     * @param loginUser 当前登录用户快照。
     * @return 工单数据范围条件。
     */
    public TicketDataScopeCriteria resolve(LoginUser loginUser) {
        DataScopeType dataScope = loginUser.dataScope();
        if (dataScope == DataScopeType.ALL) {
            return TicketDataScopeCriteria.all();
        }
        if (dataScope == DataScopeType.SELF) {
            return TicketDataScopeCriteria.self(loginUser.userId());
        }
        if (dataScope == DataScopeType.DEPT) {
            return TicketDataScopeCriteria.depts(List.of(loginUser.deptId()), loginUser.userId());
        }
        return TicketDataScopeCriteria.depts(findSelfAndChildDeptIds(loginUser), loginUser.userId());
    }

    /**
     * 将当前用户的数据范围追加到工单查询条件。
     *
     * @param query 工单查询包装器。
     * @param loginUser 当前登录用户快照。
     */
    public void apply(LambdaQueryWrapper<Ticket> query, LoginUser loginUser) {
        resolve(loginUser).applyTo(query);
    }

    private List<Long> findSelfAndChildDeptIds(LoginUser loginUser) {
        if (!StringUtils.hasText(loginUser.deptPath())) {
            return loginUser.deptId() == null ? List.of() : List.of(loginUser.deptId());
        }
        List<Long> deptIds = deptMapper.selectList(Wrappers.<SysDept>lambdaQuery()
                        .likeRight(SysDept::getPath, loginUser.deptPath()))
                .stream()
                .map(SysDept::getId)
                .toList();
        return deptIds.isEmpty() && loginUser.deptId() != null ? List.of(loginUser.deptId()) : deptIds;
    }
}
