package com.ticketflow.rule.service;

import com.ticketflow.rule.domain.AutoDispatchResult;
import com.ticketflow.ticket.entity.Ticket;

/**
 * 自动派单服务接口。
 *
 * <p>负责在工单创建后根据分类、部门、派单规则和当前处理人负载计算处理人。</p>
 */
public interface AutoDispatchService {

    /**
     * 为工单计算自动派单结果。
     *
     * @param ticket 待派单工单。
     * @return 自动派单结果。
     */
    AutoDispatchResult dispatch(Ticket ticket);
}
