package com.ticketflow.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.common.cache.RedisJsonCacheService;
import com.ticketflow.common.cache.TicketFlowCacheKeys;
import com.ticketflow.common.context.CurrentUserContext;
import com.ticketflow.common.context.LoginUser;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.common.web.PageResult;
import com.ticketflow.rule.domain.AutoDispatchResult;
import com.ticketflow.rule.service.AutoDispatchService;
import com.ticketflow.sla.domain.SlaCalculator;
import com.ticketflow.sla.domain.SlaDeadline;
import com.ticketflow.sla.entity.SlaRule;
import com.ticketflow.sla.mapper.SlaRuleMapper;
import com.ticketflow.system.domain.PermissionMatcher;
import com.ticketflow.system.entity.SysDept;
import com.ticketflow.system.entity.SysUser;
import com.ticketflow.system.mapper.SysDeptMapper;
import com.ticketflow.system.mapper.SysUserMapper;
import com.ticketflow.ticket.domain.TicketDataScopeSupport;
import com.ticketflow.ticket.domain.TicketWorkflowEngine;
import com.ticketflow.ticket.dto.TicketActionRequest;
import com.ticketflow.ticket.dto.TicketCommentRequest;
import com.ticketflow.ticket.dto.TicketCommentResponse;
import com.ticketflow.ticket.dto.TicketCreateRequest;
import com.ticketflow.ticket.dto.TicketDetailResponse;
import com.ticketflow.ticket.dto.TicketFlowRecordResponse;
import com.ticketflow.ticket.dto.TicketHandleRequest;
import com.ticketflow.ticket.dto.TicketListItemResponse;
import com.ticketflow.ticket.dto.TicketTransferRequest;
import com.ticketflow.ticket.entity.Ticket;
import com.ticketflow.ticket.entity.TicketComment;
import com.ticketflow.ticket.entity.TicketCategory;
import com.ticketflow.ticket.entity.TicketFlowRecord;
import com.ticketflow.ticket.enums.TicketAllowedAction;
import com.ticketflow.ticket.enums.TicketStatus;
import com.ticketflow.ticket.mapper.TicketCategoryMapper;
import com.ticketflow.ticket.mapper.TicketCommentMapper;
import com.ticketflow.ticket.mapper.TicketFlowRecordMapper;
import com.ticketflow.ticket.mapper.TicketMapper;
import com.ticketflow.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 工单服务实现类。
 *
 * <p>负责工单主流程的持久化编排：创建工单、写入流转记录、保存评论、接单、处理、
 * 转派、确认关闭、驳回和取消。具体状态合法性由 {@link TicketWorkflowEngine} 统一判断，
 * 服务层只负责读取当前登录用户、保存数据库并组装响应 DTO。</p>
 */
@Service
@RequiredArgsConstructor
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements TicketService {

    private final TicketFlowRecordMapper flowRecordMapper;
    private final TicketCommentMapper commentMapper;
    private final TicketDataScopeSupport ticketDataScopeSupport;
    private final AutoDispatchService autoDispatchService;
    private final SlaRuleMapper slaRuleMapper;
    private final RedisJsonCacheService cacheService;
    private final TicketCategoryMapper categoryMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    @Override
    public PageResult<TicketListItemResponse> pageTickets(long pageNo, long pageSize, TicketStatus status, String keyword) {
        LoginUser loginUser = CurrentUserContext.getRequired();
        LambdaQueryWrapper<Ticket> query = Wrappers.lambdaQuery(Ticket.class);
        if (status != null) {
            query.eq(Ticket::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(Ticket::getTicketNo, keyword)
                    .or()
                    .like(Ticket::getTitle, keyword));
        }
        ticketDataScopeSupport.apply(query, loginUser);
        query.orderByDesc(Ticket::getCreatedAt).orderByDesc(Ticket::getId);
        IPage<Ticket> page = page(Page.of(pageNo, pageSize), query);
        TicketDisplayContext displayContext = buildDisplayContext(page.getRecords());
        List<TicketListItemResponse> records = page.getRecords().stream()
                .map(ticket -> toListItemResponse(ticket, displayContext, loginUser))
                .toList();
        return new PageResult<>(records, page.getTotal(), pageNo, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketDetailResponse createTicket(TicketCreateRequest request) {
        LoginUser loginUser = CurrentUserContext.getRequired();
        Ticket ticket = new Ticket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setCategoryId(request.categoryId());
        ticket.setPriority(request.priority());
        ticket.setStatus(TicketStatus.PENDING_ASSIGN);
        ticket.setCreatorId(loginUser.userId());
        ticket.setCreatorDeptId(loginUser.deptId());
        applySlaDeadline(ticket);
        save(ticket);

        TicketFlowRecord record = new TicketFlowRecord();
        record.setTicketId(ticket.getId());
        record.setFromStatus(TicketStatus.CREATED);
        record.setToStatus(TicketStatus.PENDING_ASSIGN);
        record.setOperatorId(loginUser.userId());
        record.setRemark("创建工单并进入待分派");
        flowRecordMapper.insert(record);
        applyAutoDispatch(ticket, loginUser.userId());
        evictHotStats();
        return detail(ticket.getId());
    }

    @Override
    public TicketDetailResponse detail(Long id) {
        Ticket ticket = getTicketRequired(id);
        List<TicketFlowRecordResponse> flowRecords = flowRecordMapper.selectList(Wrappers.<TicketFlowRecord>lambdaQuery()
                        .eq(TicketFlowRecord::getTicketId, id)
                        .orderByAsc(TicketFlowRecord::getCreatedAt)
                        .orderByAsc(TicketFlowRecord::getId))
                .stream()
                .map(this::toFlowRecordResponse)
                .toList();
        List<TicketCommentResponse> comments = commentMapper.selectList(Wrappers.<TicketComment>lambdaQuery()
                        .eq(TicketComment::getTicketId, id)
                        .orderByAsc(TicketComment::getCreatedAt)
                        .orderByAsc(TicketComment::getId))
                .stream()
                .map(this::toCommentResponse)
                .toList();
        return toDetailResponse(ticket, flowRecords, comments, buildDisplayContext(List.of(ticket)), CurrentUserContext.getRequired());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketDetailResponse accept(Long id) {
        LoginUser loginUser = CurrentUserContext.getRequired();
        Ticket ticket = getTicketRequired(id);
        TicketFlowRecord record = TicketWorkflowEngine.accept(ticket, loginUser.userId(), LocalDateTime.now());
        updateById(ticket);
        flowRecordMapper.insert(record);
        evictHotStats();
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketDetailResponse submitResult(Long id, TicketHandleRequest request) {
        LoginUser loginUser = CurrentUserContext.getRequired();
        Ticket ticket = getTicketRequired(id);
        TicketFlowRecord record = TicketWorkflowEngine.submitResult(ticket, loginUser.userId(), request.result());
        updateById(ticket);
        flowRecordMapper.insert(record);
        evictHotStats();
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketDetailResponse transfer(Long id, TicketTransferRequest request) {
        LoginUser loginUser = CurrentUserContext.getRequired();
        Ticket ticket = getTicketRequired(id);
        String reason = StringUtils.hasText(request.reason()) ? request.reason() : "主管转派工单";
        TicketFlowRecord record = TicketWorkflowEngine.transfer(ticket, request.assigneeId(), loginUser.userId(), reason);
        updateById(ticket);
        flowRecordMapper.insert(record);
        evictHotStats();
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketDetailResponse confirmClose(Long id, TicketActionRequest request) {
        LoginUser loginUser = CurrentUserContext.getRequired();
        Ticket ticket = getTicketRequired(id);
        TicketFlowRecord record = TicketWorkflowEngine.confirmClose(ticket, loginUser.userId(), LocalDateTime.now());
        if (request != null && StringUtils.hasText(request.remark())) {
            record.setRemark(request.remark());
        }
        updateById(ticket);
        flowRecordMapper.insert(record);
        evictHotStats();
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketDetailResponse reject(Long id, TicketActionRequest request) {
        LoginUser loginUser = CurrentUserContext.getRequired();
        Ticket ticket = getTicketRequired(id);
        String reason = request == null || !StringUtils.hasText(request.remark()) ? "处理结果被驳回" : request.remark();
        TicketFlowRecord record = TicketWorkflowEngine.reject(ticket, loginUser.userId(), reason);
        updateById(ticket);
        flowRecordMapper.insert(record);
        evictHotStats();
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketDetailResponse cancel(Long id, TicketActionRequest request) {
        LoginUser loginUser = CurrentUserContext.getRequired();
        Ticket ticket = getTicketRequired(id);
        String reason = request == null || !StringUtils.hasText(request.remark()) ? "取消工单" : request.remark();
        TicketFlowRecord record = TicketWorkflowEngine.cancel(ticket, loginUser.userId(), reason);
        updateById(ticket);
        flowRecordMapper.insert(record);
        evictHotStats();
        return detail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketDetailResponse addComment(Long id, TicketCommentRequest request) {
        LoginUser loginUser = CurrentUserContext.getRequired();
        getTicketRequired(id);
        TicketComment comment = new TicketComment();
        comment.setTicketId(id);
        comment.setUserId(loginUser.userId());
        comment.setContent(request.content());
        comment.setInternalOnly(Boolean.TRUE.equals(request.internalOnly()) ? 1 : 0);
        commentMapper.insert(comment);
        return detail(id);
    }

    private Ticket getTicketRequired(Long id) {
        Ticket ticket = getById(id);
        if (ticket == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工单不存在");
        }
        LoginUser loginUser = CurrentUserContext.getRequired();
        if (!ticketDataScopeSupport.resolve(loginUser).matches(ticket)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有访问该工单的权限");
        }
        return ticket;
    }

    private String generateTicketNo() {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return "TF" + timePart + random;
    }

    private void applySlaDeadline(Ticket ticket) {
        if (ticket.getPriority() == null) {
            return;
        }
        SlaRule rule = slaRuleMapper.selectOne(Wrappers.<SlaRule>lambdaQuery()
                .eq(SlaRule::getPriority, ticket.getPriority())
                .eq(SlaRule::getEnabled, 1)
                .last("limit 1"));
        if (rule == null) {
            return;
        }
        SlaDeadline deadline = SlaCalculator.calculate(
                LocalDateTime.now(),
                rule.getResponseMinutes(),
                rule.getResolveMinutes()
        );
        ticket.setResponseDeadline(deadline.responseDeadline());
        ticket.setResolveDeadline(deadline.resolveDeadline());
    }

    private void applyAutoDispatch(Ticket ticket, Long operatorId) {
        AutoDispatchResult result = autoDispatchService.dispatch(ticket);
        if (!result.assigned()) {
            return;
        }
        TicketStatus fromStatus = ticket.getStatus();
        ticket.setAssigneeId(result.assigneeId());
        ticket.setStatus(result.targetStatus());
        updateById(ticket);

        TicketFlowRecord dispatchRecord = new TicketFlowRecord();
        dispatchRecord.setTicketId(ticket.getId());
        dispatchRecord.setFromStatus(fromStatus);
        dispatchRecord.setToStatus(result.targetStatus());
        dispatchRecord.setOperatorId(operatorId);
        dispatchRecord.setRemark(result.reason());
        flowRecordMapper.insert(dispatchRecord);
    }

    private void evictHotStats() {
        cacheService.deleteByPattern(TicketFlowCacheKeys.hotStatsPattern());
    }

    private TicketDetailResponse toDetailResponse(
            Ticket ticket,
            List<TicketFlowRecordResponse> flowRecords,
            List<TicketCommentResponse> comments,
            TicketDisplayContext displayContext,
            LoginUser loginUser
    ) {
        return new TicketDetailResponse(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getCategoryId(),
                displayContext.categoryName(ticket.getCategoryId()),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatorId(),
                displayContext.userName(ticket.getCreatorId()),
                ticket.getCreatorDeptId(),
                displayContext.deptName(ticket.getCreatorDeptId()),
                ticket.getAssigneeId(),
                displayContext.userName(ticket.getAssigneeId()),
                ticket.getResponseDeadline(),
                ticket.getResolveDeadline(),
                ticket.getRespondedAt(),
                ticket.getClosedAt(),
                flowRecords,
                comments,
                allowedActions(ticket, loginUser)
        );
    }

    private TicketListItemResponse toListItemResponse(
            Ticket ticket,
            TicketDisplayContext displayContext,
            LoginUser loginUser
    ) {
        return new TicketListItemResponse(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getTitle(),
                ticket.getCategoryId(),
                displayContext.categoryName(ticket.getCategoryId()),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatorId(),
                displayContext.userName(ticket.getCreatorId()),
                displayContext.deptName(ticket.getCreatorDeptId()),
                ticket.getAssigneeId(),
                displayContext.userName(ticket.getAssigneeId()),
                ticket.getResponseDeadline(),
                ticket.getResolveDeadline(),
                ticket.getCreatedAt(),
                allowedActions(ticket, loginUser)
        );
    }

    private TicketDisplayContext buildDisplayContext(List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return new TicketDisplayContext(Map.of(), Map.of(), Map.of());
        }
        Set<Long> categoryIds = tickets.stream()
                .map(Ticket::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> userIds = tickets.stream()
                .flatMap(ticket -> Stream.of(ticket.getCreatorId(), ticket.getAssigneeId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> deptIds = new HashSet<>();
        tickets.stream()
                .map(Ticket::getCreatorDeptId)
                .filter(Objects::nonNull)
                .forEach(deptIds::add);

        Map<Long, TicketCategory> categories = categoryIds.isEmpty()
                ? Map.of()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(TicketCategory::getId, Function.identity()));
        Map<Long, SysUser> users = userIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        users.values().stream()
                .map(SysUser::getDeptId)
                .filter(Objects::nonNull)
                .forEach(deptIds::add);
        Map<Long, SysDept> depts = deptIds.isEmpty()
                ? Map.of()
                : deptMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(SysDept::getId, Function.identity()));
        return new TicketDisplayContext(categories, users, depts);
    }

    private List<TicketAllowedAction> allowedActions(Ticket ticket, LoginUser loginUser) {
        List<TicketAllowedAction> actions = new ArrayList<>();
        Long userId = loginUser.userId();
        if (Objects.equals(ticket.getAssigneeId(), userId)
                && ticket.getStatus() == TicketStatus.PENDING_ACCEPT
                && hasPermission(loginUser, "ticket:accept")) {
            actions.add(TicketAllowedAction.ACCEPT);
        }
        if (Objects.equals(ticket.getAssigneeId(), userId)
                && ticket.getStatus() == TicketStatus.PROCESSING
                && hasPermission(loginUser, "ticket:process")) {
            actions.add(TicketAllowedAction.PROCESS);
        }
        if (ticket.getStatus() != TicketStatus.CLOSED
                && ticket.getStatus() != TicketStatus.CANCELED
                && hasPermission(loginUser, "ticket:transfer")) {
            actions.add(TicketAllowedAction.TRANSFER);
        }
        if (Objects.equals(ticket.getCreatorId(), userId)
                && ticket.getStatus() == TicketStatus.PENDING_CONFIRM
                && hasPermission(loginUser, "ticket:confirm-close")) {
            actions.add(TicketAllowedAction.CONFIRM_CLOSE);
            actions.add(TicketAllowedAction.REJECT);
        }
        if (Objects.equals(ticket.getCreatorId(), userId)
                && ticket.getStatus() != TicketStatus.CLOSED
                && ticket.getStatus() != TicketStatus.CANCELED
                && hasPermission(loginUser, "ticket:cancel")) {
            actions.add(TicketAllowedAction.CANCEL);
        }
        if (hasPermission(loginUser, "ticket:comment")) {
            actions.add(TicketAllowedAction.COMMENT);
        }
        if (hasPermission(loginUser, "ticket:attachment:upload")) {
            actions.add(TicketAllowedAction.UPLOAD_ATTACHMENT);
        }
        return actions;
    }

    private boolean hasPermission(LoginUser loginUser, String permission) {
        return PermissionMatcher.hasAnyPermission(loginUser.permissions(), permission);
    }

    private record TicketDisplayContext(
            Map<Long, TicketCategory> categories,
            Map<Long, SysUser> users,
            Map<Long, SysDept> depts
    ) {

        private String categoryName(Long categoryId) {
            TicketCategory category = categories.get(categoryId);
            return category == null ? null : category.getCategoryName();
        }

        private String userName(Long userId) {
            SysUser user = users.get(userId);
            return user == null ? null : user.getRealName();
        }

        private String deptName(Long deptId) {
            SysDept dept = depts.get(deptId);
            return dept == null ? null : dept.getDeptName();
        }
    }

    private TicketFlowRecordResponse toFlowRecordResponse(TicketFlowRecord record) {
        return new TicketFlowRecordResponse(
                record.getId(),
                record.getFromStatus(),
                record.getToStatus(),
                record.getOperatorId(),
                record.getRemark(),
                record.getCreatedAt()
        );
    }

    private TicketCommentResponse toCommentResponse(TicketComment comment) {
        return new TicketCommentResponse(
                comment.getId(),
                comment.getUserId(),
                comment.getContent(),
                comment.getInternalOnly(),
                comment.getCreatedAt()
        );
    }
}
