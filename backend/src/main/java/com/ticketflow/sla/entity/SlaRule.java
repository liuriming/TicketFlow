package com.ticketflow.sla.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import com.ticketflow.ticket.enums.TicketPriority;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SLA 规则实体。
 *
 * <p>用于按工单优先级配置响应时限和处理时限，定时任务会根据该规则扫描超时工单。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sla_rule")
public class SlaRule extends BaseEntity {

    /**
     * 工单优先级。
     */
    private TicketPriority priority;

    /**
     * 响应时限，单位分钟。
     */
    private Integer responseMinutes;

    /**
     * 处理时限，单位分钟。
     */
    private Integer resolveMinutes;

    /**
     * 是否启用：1 启用，0 停用。
     */
    private Integer enabled;
}
