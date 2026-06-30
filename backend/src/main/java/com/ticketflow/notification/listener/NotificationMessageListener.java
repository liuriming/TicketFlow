package com.ticketflow.notification.listener;

import com.ticketflow.notification.config.RabbitNotificationConfig;
import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知消息消费者。
 *
 * <p>监听 RabbitMQ 通知队列，并将通知事件落库为站内信。</p>
 */
@Component
@RequiredArgsConstructor
public class NotificationMessageListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitNotificationConfig.QUEUE)
    public void onMessage(NotificationEvent event) {
        notificationService.saveIfAbsent(event);
    }
}
