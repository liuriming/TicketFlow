package com.ticketflow.notification.controller;

import com.ticketflow.common.web.ApiResult;
import com.ticketflow.common.web.PageResult;
import com.ticketflow.notification.dto.NotificationMessageResponse;
import com.ticketflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 消息接口。
 *
 * <p>用于站内信列表、已读处理和未读数量查询。</p>
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResult<PageResult<NotificationMessageResponse>> page(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResult.success(notificationService.pageCurrentUserMessages(pageNo, pageSize));
    }

    @GetMapping("/unread-count")
    public ApiResult<Map<String, Long>> unreadCount() {
        return ApiResult.success(Map.of("count", notificationService.unreadCount()));
    }

    @PostMapping("/{id}/read")
    public ApiResult<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResult.success();
    }
}
