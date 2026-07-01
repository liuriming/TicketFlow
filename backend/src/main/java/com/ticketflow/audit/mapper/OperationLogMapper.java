package com.ticketflow.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketflow.audit.entity.OperationLog;

/**
 * 操作审计日志 Mapper。
 *
 * <p>负责 `operation_log` 表基础增删改查，复杂筛选由服务层通过 MyBatis-Plus 条件构造器完成。</p>
 */
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
