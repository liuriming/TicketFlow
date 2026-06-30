package com.ticketflow.ticket.controller;

import com.ticketflow.common.web.ApiResult;
import com.ticketflow.common.web.PageResult;
import com.ticketflow.system.annotation.RequirePermission;
import com.ticketflow.ticket.dto.TicketActionRequest;
import com.ticketflow.ticket.dto.TicketCommentRequest;
import com.ticketflow.ticket.dto.TicketCreateRequest;
import com.ticketflow.ticket.dto.TicketDetailResponse;
import com.ticketflow.ticket.dto.TicketHandleRequest;
import com.ticketflow.ticket.dto.TicketListItemResponse;
import com.ticketflow.ticket.dto.TicketTransferRequest;
import com.ticketflow.ticket.enums.TicketStatus;
import com.ticketflow.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工单流程接口。
 *
 * <p>提供创建、详情、接单、处理、转派、确认关闭、驳回、取消和评论等核心工单闭环接口。</p>
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@RequirePermission("ticket:list")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ApiResult<PageResult<TicketListItemResponse>> page(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResult.success(ticketService.pageTickets(pageNo, pageSize, status, keyword));
    }

    @GetMapping("/{id}")
    public ApiResult<TicketDetailResponse> detail(@PathVariable Long id) {
        return ApiResult.success(ticketService.detail(id));
    }

    @PostMapping
    public ApiResult<TicketDetailResponse> create(@Valid @RequestBody TicketCreateRequest request) {
        return ApiResult.success(ticketService.createTicket(request));
    }

    @PostMapping("/{id}/accept")
    public ApiResult<TicketDetailResponse> accept(@PathVariable Long id) {
        return ApiResult.success(ticketService.accept(id));
    }

    @PostMapping("/{id}/process")
    public ApiResult<TicketDetailResponse> process(
            @PathVariable Long id,
            @Valid @RequestBody TicketHandleRequest request
    ) {
        return ApiResult.success(ticketService.submitResult(id, request));
    }

    @PostMapping("/{id}/transfer")
    @RequirePermission("ticket:transfer")
    public ApiResult<TicketDetailResponse> transfer(
            @PathVariable Long id,
            @Valid @RequestBody TicketTransferRequest request
    ) {
        return ApiResult.success(ticketService.transfer(id, request));
    }

    @PostMapping("/{id}/confirm-close")
    public ApiResult<TicketDetailResponse> confirmClose(
            @PathVariable Long id,
            @RequestBody(required = false) TicketActionRequest request
    ) {
        return ApiResult.success(ticketService.confirmClose(id, request));
    }

    @PostMapping("/{id}/reject")
    public ApiResult<TicketDetailResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) TicketActionRequest request
    ) {
        return ApiResult.success(ticketService.reject(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResult<TicketDetailResponse> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) TicketActionRequest request
    ) {
        return ApiResult.success(ticketService.cancel(id, request));
    }

    @PostMapping("/{id}/comments")
    public ApiResult<TicketDetailResponse> comment(
            @PathVariable Long id,
            @Valid @RequestBody TicketCommentRequest request
    ) {
        return ApiResult.success(ticketService.addComment(id, request));
    }
}
