package com.ticketflow.attachment.domain;

import com.ticketflow.common.context.CurrentUserContext;
import com.ticketflow.common.context.LoginUser;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.system.enums.DataScopeType;
import com.ticketflow.ticket.domain.TicketDataScopeCriteria;
import com.ticketflow.ticket.domain.TicketDataScopeSupport;
import com.ticketflow.ticket.entity.Ticket;
import com.ticketflow.ticket.mapper.TicketMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttachmentBusinessAccessValidatorTest {

    private final TicketMapper ticketMapper = mock(TicketMapper.class);
    private final TicketDataScopeSupport ticketDataScopeSupport = mock(TicketDataScopeSupport.class);
    private final AttachmentBusinessAccessValidator validator = new AttachmentBusinessAccessValidator(ticketMapper, ticketDataScopeSupport);

    @AfterEach
    void tearDown() {
        CurrentUserContext.clear();
    }

    @Test
    void 非工单附件不需要校验工单数据范围() {
        validator.validateBusinessAccess("TEMP", null);

        verify(ticketMapper, never()).selectById(1L);
    }

    @Test
    void 附件列表查询必须指定明确业务对象() {
        assertThatThrownBy(() -> validator.validateListAccess(null, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BUSINESS_ERROR);
    }

    @Test
    void 工单附件命中当前用户数据范围时允许访问() {
        LoginUser loginUser = loginUser(3L);
        CurrentUserContext.set(loginUser);
        Ticket ticket = ticket(2L, 3L);
        when(ticketMapper.selectById(1L)).thenReturn(ticket);
        when(ticketDataScopeSupport.resolve(loginUser)).thenReturn(TicketDataScopeCriteria.depts(List.of(2L), 3L));

        assertThatCode(() -> validator.validateBusinessAccess("TICKET", 1L)).doesNotThrowAnyException();
    }

    @Test
    void 工单附件未命中当前用户数据范围时拒绝访问() {
        LoginUser loginUser = loginUser(3L);
        CurrentUserContext.set(loginUser);
        Ticket ticket = ticket(4L, 8L);
        when(ticketMapper.selectById(1L)).thenReturn(ticket);
        when(ticketDataScopeSupport.resolve(loginUser)).thenReturn(TicketDataScopeCriteria.depts(List.of(2L), 3L));

        assertThatThrownBy(() -> validator.validateBusinessAccess("TICKET", 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private LoginUser loginUser(Long userId) {
        return new LoginUser(userId, "user" + userId, "用户" + userId, 2L, "1/2", DataScopeType.DEPT, List.of("OPS_ENGINEER"), List.of("ticket:list"));
    }

    private Ticket ticket(Long creatorDeptId, Long assigneeId) {
        Ticket ticket = new Ticket();
        ticket.setCreatorId(9L);
        ticket.setCreatorDeptId(creatorDeptId);
        ticket.setAssigneeId(assigneeId);
        return ticket;
    }
}
