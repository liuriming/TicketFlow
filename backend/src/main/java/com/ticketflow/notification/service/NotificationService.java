package com.ticketflow.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketflow.common.web.PageResult;
import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.notification.dto.NotificationMessageResponse;
import com.ticketflow.notification.entity.NotificationMessage;

/**
 * 站内信服务接口。
 *
 * <p>封装消息落库、列表查询、已读处理和未读数量统计。</p>
 */
public interface NotificationService extends IService<NotificationMessage> {

    /**
     * 保存通知事件，已存在的同业务消息不会重复保存。
     *
     * @param event 通知事件。
     */
    void saveIfAbsent(NotificationEvent event);

    /**
     * 查询当前用户站内信。
     *
     * @param pageNo 当前页码。
     * @param pageSize 每页数量。
     * @return 站内信分页列表。
     */
    PageResult<NotificationMessageResponse> pageCurrentUserMessages(long pageNo, long pageSize);

    /**
     * 标记消息为已读。
     *
     * @param id 消息 ID。
     */
    void markRead(Long id);

    /**
     * 查询当前用户未读数量。
     *
     * @return 未读数量。
     */
    long unreadCount();
}
