package com.ticketflow.sla.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.sla.entity.SlaAlertRecord;
import com.ticketflow.sla.enums.SlaAlertType;
import com.ticketflow.sla.mapper.SlaAlertRecordMapper;
import com.ticketflow.sla.service.SlaAlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * SLA 告警记录服务实现类。
 *
 * <p>基于工单 ID 和告警类型判断是否已经发布过提醒。当前实现先查询再插入，
 * 数据库唯一索引 `uk_sla_alert_ticket_type` 作为最终重复保护。</p>
 */
@Service
public class SlaAlertServiceImpl extends ServiceImpl<SlaAlertRecordMapper, SlaAlertRecord> implements SlaAlertService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markPublishedIfAbsent(Long ticketId, SlaAlertType alertType) {
        Long count = count(Wrappers.<SlaAlertRecord>lambdaQuery()
                .eq(SlaAlertRecord::getTicketId, ticketId)
                .eq(SlaAlertRecord::getAlertType, alertType));
        if (count > 0) {
            return false;
        }
        SlaAlertRecord record = new SlaAlertRecord();
        record.setTicketId(ticketId);
        record.setAlertType(alertType);
        record.setPublishedAt(LocalDateTime.now());
        save(record);
        return true;
    }
}
