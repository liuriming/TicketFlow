package com.ticketflow.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketflow.ticket.entity.TicketFlowRecord;

/**
 * 工单流转记录 Mapper。
 *
 * <p>负责保存和查询工单状态流转、转派、驳回、关闭等操作日志。</p>
 */
public interface TicketFlowRecordMapper extends BaseMapper<TicketFlowRecord> {
}
