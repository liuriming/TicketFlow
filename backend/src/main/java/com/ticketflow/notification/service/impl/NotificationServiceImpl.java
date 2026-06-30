package com.ticketflow.notification.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.common.context.CurrentUserContext;
import com.ticketflow.common.context.LoginUser;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.common.web.PageResult;
import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.notification.dto.NotificationMessageResponse;
import com.ticketflow.notification.entity.NotificationMessage;
import com.ticketflow.notification.mapper.NotificationMessageMapper;
import com.ticketflow.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 站内信服务实现类。
 *
 * <p>负责通知消息幂等落库、当前用户消息列表、未读数量统计和标记已读。</p>
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMessageMapper, NotificationMessage> implements NotificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveIfAbsent(NotificationEvent event) {
        Long count = count(Wrappers.<NotificationMessage>lambdaQuery()
                .eq(NotificationMessage::getReceiverId, event.receiverId())
                .eq(NotificationMessage::getBusinessType, event.businessType())
                .eq(NotificationMessage::getBusinessId, event.businessId()));
        if (count > 0) {
            return;
        }
        NotificationMessage message = new NotificationMessage();
        message.setReceiverId(event.receiverId());
        message.setTitle(event.title());
        message.setContent(event.content());
        message.setBusinessType(event.businessType());
        message.setBusinessId(event.businessId());
        message.setReadFlag(0);
        save(message);
    }

    @Override
    public PageResult<NotificationMessageResponse> pageCurrentUserMessages(long pageNo, long pageSize) {
        LoginUser loginUser = CurrentUserContext.getRequired();
        IPage<NotificationMessage> page = page(Page.of(pageNo, pageSize), Wrappers.<NotificationMessage>lambdaQuery()
                .eq(NotificationMessage::getReceiverId, loginUser.userId())
                .orderByAsc(NotificationMessage::getReadFlag)
                .orderByDesc(NotificationMessage::getCreatedAt));
        List<NotificationMessageResponse> records = page.getRecords().stream()
                .map(this::toResponse)
                .toList();
        return new PageResult<>(records, page.getTotal(), pageNo, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id) {
        LoginUser loginUser = CurrentUserContext.getRequired();
        NotificationMessage message = getById(id);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "消息不存在");
        }
        if (!loginUser.userId().equals(message.getReceiverId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能操作他人的消息");
        }
        message.setReadFlag(1);
        updateById(message);
    }

    @Override
    public long unreadCount() {
        LoginUser loginUser = CurrentUserContext.getRequired();
        return count(Wrappers.<NotificationMessage>lambdaQuery()
                .eq(NotificationMessage::getReceiverId, loginUser.userId())
                .eq(NotificationMessage::getReadFlag, 0));
    }

    private NotificationMessageResponse toResponse(NotificationMessage message) {
        return new NotificationMessageResponse(
                message.getId(),
                message.getReceiverId(),
                message.getTitle(),
                message.getContent(),
                message.getBusinessType(),
                message.getBusinessId(),
                message.getReadFlag(),
                message.getCreatedAt()
        );
    }
}
