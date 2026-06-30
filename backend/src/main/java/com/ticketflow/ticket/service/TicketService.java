package com.ticketflow.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketflow.common.web.PageResult;
import com.ticketflow.ticket.dto.TicketActionRequest;
import com.ticketflow.ticket.dto.TicketCommentRequest;
import com.ticketflow.ticket.dto.TicketCreateRequest;
import com.ticketflow.ticket.dto.TicketDetailResponse;
import com.ticketflow.ticket.dto.TicketHandleRequest;
import com.ticketflow.ticket.dto.TicketListItemResponse;
import com.ticketflow.ticket.dto.TicketTransferRequest;
import com.ticketflow.ticket.entity.Ticket;
import com.ticketflow.ticket.enums.TicketStatus;

/**
 * 工单服务接口。
 *
 * <p>封装工单创建、详情、接单、处理、转派、确认关闭、驳回、取消和评论等核心流程。</p>
 */
public interface TicketService extends IService<Ticket> {

    /**
     * 分页查询工单。
     *
     * @param pageNo 当前页码。
     * @param pageSize 每页数量。
     * @param status 工单状态，可为空。
     * @param keyword 关键字，可匹配工单编号和标题。
     * @return 工单分页列表。
     */
    PageResult<TicketListItemResponse> pageTickets(long pageNo, long pageSize, TicketStatus status, String keyword);

    /**
     * 创建工单。
     *
     * @param request 创建工单请求。
     * @return 工单详情。
     */
    TicketDetailResponse createTicket(TicketCreateRequest request);

    /**
     * 查询工单详情。
     *
     * @param id 工单 ID。
     * @return 工单详情。
     */
    TicketDetailResponse detail(Long id);

    /**
     * 当前处理人接单。
     *
     * @param id 工单 ID。
     * @return 工单详情。
     */
    TicketDetailResponse accept(Long id);

    /**
     * 提交处理结果。
     *
     * @param id 工单 ID。
     * @param request 处理请求。
     * @return 工单详情。
     */
    TicketDetailResponse submitResult(Long id, TicketHandleRequest request);

    /**
     * 转派工单。
     *
     * @param id 工单 ID。
     * @param request 转派请求。
     * @return 工单详情。
     */
    TicketDetailResponse transfer(Long id, TicketTransferRequest request);

    /**
     * 确认关闭工单。
     *
     * @param id 工单 ID。
     * @param request 操作请求。
     * @return 工单详情。
     */
    TicketDetailResponse confirmClose(Long id, TicketActionRequest request);

    /**
     * 驳回处理结果。
     *
     * @param id 工单 ID。
     * @param request 操作请求。
     * @return 工单详情。
     */
    TicketDetailResponse reject(Long id, TicketActionRequest request);

    /**
     * 取消工单。
     *
     * @param id 工单 ID。
     * @param request 操作请求。
     * @return 工单详情。
     */
    TicketDetailResponse cancel(Long id, TicketActionRequest request);

    /**
     * 添加工单评论。
     *
     * @param id 工单 ID。
     * @param request 评论请求。
     * @return 工单详情。
     */
    TicketDetailResponse addComment(Long id, TicketCommentRequest request);
}
