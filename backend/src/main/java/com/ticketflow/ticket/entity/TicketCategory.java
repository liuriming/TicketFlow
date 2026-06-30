package com.ticketflow.ticket.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单分类实体。
 *
 * <p>分类用于员工创建工单时选择问题类型，也用于自动派单规则和 SLA 规则匹配。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ticket_category")
public class TicketCategory extends BaseEntity {

    /**
     * 父分类 ID，根分类为 0。
     */
    private Long parentId;

    /**
     * 分类名称，例如网络故障、账号权限、硬件维修。
     */
    private String categoryName;

    /**
     * 分类编码，用于规则匹配。
     */
    private String categoryCode;

    /**
     * 显示排序，数值越小越靠前。
     */
    private Integer sortOrder;

    /**
     * 是否启用：1 启用，0 停用。
     */
    private Integer enabled;
}
