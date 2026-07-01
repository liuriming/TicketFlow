package com.ticketflow.sla.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketflow.sla.entity.SlaAlertRecord;
import com.ticketflow.sla.enums.SlaAlertType;

/**
 * SLA 告警记录服务。
 *
 * <p>用于判断某个工单的某类 SLA 告警是否已经发布，避免定时任务重复推送站内信。</p>
 */
public interface SlaAlertService extends IService<SlaAlertRecord> {

    /**
     * 首次命中时登记告警并返回 true，已经登记过则返回 false。
     *
     * @param ticketId 工单 ID。
     * @param alertType 告警类型。
     * @return 是否为首次登记。
     */
    boolean markPublishedIfAbsent(Long ticketId, SlaAlertType alertType);
}
