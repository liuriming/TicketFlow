package com.ticketflow.common.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库实体公共字段。
 *
 * <p>所有业务实体统一继承该类，用于保证主键、创建时间、更新时间和逻辑删除字段含义一致。
 * 这些字段由数据库或 MyBatis-Plus 自动填充，业务代码不应随意覆盖。</p>
 */
@Data
public abstract class BaseEntity {

    /**
     * 主键 ID，使用数据库自增策略。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 记录创建时间，用于审计和列表排序。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 记录最后更新时间，用于并发排查和数据变更追踪。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记：0 表示未删除，1 表示已删除。
     */
    @TableLogic
    private Integer deleted;
}
