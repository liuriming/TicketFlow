package com.ticketflow.attachment.domain;

import com.ticketflow.common.context.CurrentUserContext;
import com.ticketflow.common.context.LoginUser;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.ticket.domain.TicketDataScopeSupport;
import com.ticketflow.ticket.entity.Ticket;
import com.ticketflow.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 附件业务访问校验器。
 *
 * <p>附件本身只保存业务类型和业务 ID，因此访问 TICKET 类型附件时必须回到工单数据范围判断。
 * 这样可以避免用户绕过工单详情接口，直接通过附件业务 ID 查询、下载或删除无权访问的工单附件。</p>
 */
@Component
@RequiredArgsConstructor
public class AttachmentBusinessAccessValidator {

    private static final String TICKET_BUSINESS_TYPE = "TICKET";

    private final TicketMapper ticketMapper;
    private final TicketDataScopeSupport ticketDataScopeSupport;

    /**
     * 校验当前用户是否可以访问指定业务对象的附件。
     *
     * @param businessType 业务类型。
     * @param businessId 业务 ID。
     */
    public void validateBusinessAccess(String businessType, Long businessId) {
        if (!StringUtils.hasText(businessType) || !TICKET_BUSINESS_TYPE.equalsIgnoreCase(businessType)) {
            return;
        }
        if (businessId == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "工单附件必须绑定工单 ID");
        }
        Ticket ticket = ticketMapper.selectById(businessId);
        if (ticket == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单不存在");
        }
        LoginUser loginUser = CurrentUserContext.getRequired();
        if (!ticketDataScopeSupport.resolve(loginUser).matches(ticket)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有访问该工单附件的权限");
        }
    }

    /**
     * 校验附件列表查询是否指定了明确业务对象，并继续复用业务访问权限判断。
     *
     * <p>列表查询不能像上传临时附件一样允许空业务参数，否则会因为查询条件缺失而返回全量附件元数据。
     * 因此这里单独收口列表入口的参数要求，同时保留 {@link #validateBusinessAccess(String, Long)} 对 TEMP 附件上传的放行能力。</p>
     *
     * @param businessType 业务类型。
     * @param businessId 业务 ID。
     */
    public void validateListAccess(String businessType, Long businessId) {
        if (!StringUtils.hasText(businessType) || businessId == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "查询附件列表必须指定业务类型和业务 ID");
        }
        validateBusinessAccess(businessType, businessId);
    }
}
