package com.ticketflow.notification.service.impl;

import com.ticketflow.notification.config.RabbitNotificationConfig;
import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.notification.service.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 通知事件发布器。
 *
 * <p>发布失败时只记录错误日志，避免 SLA 扫描任务因单条消息发送失败而整体中断。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitNotificationPublisher implements NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitNotificationConfig.EXCHANGE,
                    RabbitNotificationConfig.ROUTING_KEY,
                    event
            );
        } catch (Exception exception) {
            log.error("通知消息发送失败，业务类型：{}，业务 ID：{}", event.businessType(), event.businessId(), exception);
        }
    }
}
