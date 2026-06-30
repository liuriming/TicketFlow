package com.ticketflow.report.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ticketflow.common.cache.RedisJsonCacheService;
import com.ticketflow.common.cache.TicketFlowCacheKeys;
import com.ticketflow.common.context.CurrentUserContext;
import com.ticketflow.common.context.LoginUser;
import com.ticketflow.report.domain.ReportCalculator;
import com.ticketflow.report.domain.ReportOverview;
import com.ticketflow.report.dto.ReportOverviewResponse;
import com.ticketflow.report.dto.WorkloadResponse;
import com.ticketflow.report.service.ReportService;
import com.ticketflow.ticket.domain.TicketDataScopeSupport;
import com.ticketflow.ticket.entity.Ticket;
import com.ticketflow.ticket.enums.TicketStatus;
import com.ticketflow.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * 报表服务实现类。
 *
 * <p>当前版本直接基于工单表聚合统计，后续数据量增大后可替换为定时汇总表或 Redis 热点统计缓存。</p>
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TicketMapper ticketMapper;
    private final TicketDataScopeSupport ticketDataScopeSupport;
    private final RedisJsonCacheService cacheService;

    private static final Duration HOT_STATS_CACHE_TTL = Duration.ofMinutes(2);

    @Override
    public ReportOverviewResponse overview() {
        LoginUser loginUser = CurrentUserContext.getRequired();
        String cacheKey = TicketFlowCacheKeys.hotStats("report:overview:user:" + loginUser.userId());
        return cacheService.get(cacheKey, ReportOverviewResponse.class)
                .orElseGet(() -> {
                    ReportOverviewResponse response = buildOverview(loginUser);
                    cacheService.put(cacheKey, response, HOT_STATS_CACHE_TTL);
                    return response;
                });
    }

    private ReportOverviewResponse buildOverview(LoginUser loginUser) {
        var query = Wrappers.lambdaQuery(Ticket.class);
        ticketDataScopeSupport.apply(query, loginUser);
        List<Ticket> tickets = ticketMapper.selectList(query);
        ReportOverview overview = ReportCalculator.overview(tickets);
        long activeAssigneeCount = tickets.stream()
                .map(Ticket::getAssigneeId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        return new ReportOverviewResponse(
                overview.ticketCount(),
                overview.overdueRate(),
                overview.averageResolveHours(),
                activeAssigneeCount
        );
    }

    @Override
    public List<WorkloadResponse> workload() {
        LoginUser loginUser = CurrentUserContext.getRequired();
        String cacheKey = TicketFlowCacheKeys.hotStats("report:workload:user:" + loginUser.userId());
        return cacheService.get(cacheKey, new TypeReference<List<WorkloadResponse>>() {
        }).orElseGet(() -> {
            List<WorkloadResponse> response = buildWorkload(loginUser);
            cacheService.put(cacheKey, response, HOT_STATS_CACHE_TTL);
            return response;
        });
    }

    private List<WorkloadResponse> buildWorkload(LoginUser loginUser) {
        var query = Wrappers.<Ticket>lambdaQuery().isNotNull(Ticket::getAssigneeId);
        ticketDataScopeSupport.apply(query, loginUser);
        List<Ticket> tickets = ticketMapper.selectList(query);
        Map<Long, List<Ticket>> groupByAssignee = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getAssigneeId));
        return groupByAssignee.entrySet().stream()
                .map(entry -> toWorkload(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(WorkloadResponse::processingCount).reversed())
                .toList();
    }

    private WorkloadResponse toWorkload(Long assigneeId, List<Ticket> tickets) {
        long processingCount = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.PROCESSING
                        || ticket.getStatus() == TicketStatus.PENDING_CONFIRM
                        || ticket.getStatus() == TicketStatus.PENDING_ACCEPT)
                .count();
        long closedCount = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.CLOSED)
                .count();
        long overdueCount = tickets.stream()
                .filter(ticket -> ticket.getClosedAt() != null
                        && ticket.getResolveDeadline() != null
                        && ticket.getClosedAt().isAfter(ticket.getResolveDeadline()))
                .count();
        return new WorkloadResponse(assigneeId, processingCount, closedCount, overdueCount);
    }
}
