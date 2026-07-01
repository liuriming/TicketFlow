package com.ticketflow.sla.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketflow.sla.entity.SlaAlertRecord;

/**
 * SLA 告警记录 Mapper。
 *
 * <p>负责 `sla_alert_record` 表的基础增删改查，服务层基于唯一索引控制重复告警。</p>
 */
public interface SlaAlertRecordMapper extends BaseMapper<SlaAlertRecord> {
}
