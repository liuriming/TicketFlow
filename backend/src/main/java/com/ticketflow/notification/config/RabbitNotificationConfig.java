package com.ticketflow.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 通知消息 RabbitMQ 配置。
 *
 * <p>SLA 定时任务将通知事件投递到交换机，消费者异步落库为站内信。</p>
 */
@Configuration
public class RabbitNotificationConfig {

    /**
     * 通知交换机名称。
     */
    public static final String EXCHANGE = "ticketflow.notification.exchange";

    /**
     * 通知队列名称。
     */
    public static final String QUEUE = "ticketflow.notification.queue";

    /**
     * 通知路由键。
     */
    public static final String ROUTING_KEY = "ticketflow.notification";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(notificationExchange()).with(ROUTING_KEY);
    }

    /**
     * RabbitMQ 通知事件 JSON 转换器。
     *
     * <p>默认 SimpleMessageConverter 会对对象使用 Java 序列化，消费端需要配置反序列化白名单。
     * 通知事件只承载 DTO 字段，使用 JSON 更适合跨进程传递，也能避免放开不必要的 Java 反序列化权限。</p>
     *
     * @return RabbitTemplate 和监听容器共用的 JSON 消息转换器。
     */
    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
