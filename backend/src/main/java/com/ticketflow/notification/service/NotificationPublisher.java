package com.ticketflow.notification.service;

import com.ticketflow.notification.dto.NotificationEvent;

/**
 * 通知事件发布器。
 *
 * <p>用于将 SLA 超时等业务事件异步投递到 RabbitMQ。</p>
 */
public interface NotificationPublisher {

    /**
     * 发布通知事件。
     *
     * @param event 通知事件。
     */
    void publish(NotificationEvent event);
}
