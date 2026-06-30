package com.ticketflow.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.common.cache.RedisJsonCacheService;
import com.ticketflow.common.cache.TicketFlowCacheKeys;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.common.util.PasswordHashUtil;
import com.ticketflow.system.dto.SysUserSaveRequest;
import com.ticketflow.system.dto.UserOptionResponse;
import com.ticketflow.system.entity.SysDept;
import com.ticketflow.system.entity.SysUser;
import com.ticketflow.system.entity.SysUserRole;
import com.ticketflow.system.enums.UserStatus;
import com.ticketflow.system.mapper.SysDeptMapper;
import com.ticketflow.system.mapper.SysUserMapper;
import com.ticketflow.system.mapper.SysUserRoleMapper;
import com.ticketflow.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户服务实现类。
 *
 * <p>负责用户基础信息维护、密码加盐摘要和用户角色关系维护。
 * 新增用户时如果未指定密码，默认使用 123456，方便本地演示和初始化数据。</p>
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysDeptMapper deptMapper;
    private final RedisJsonCacheService cacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser saveUser(Long id, SysUserSaveRequest request) {
        SysUser user = id == null ? new SysUser() : getById(id);
        if (user == null) {
            user = new SysUser();
        }
        user.setUsername(request.username());
        user.setRealName(request.realName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setDeptId(request.deptId());
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ENABLED);
        }
        if (id == null || StringUtils.hasText(request.password())) {
            String salt = UUID.randomUUID().toString().replace("-", "");
            String rawPassword = StringUtils.hasText(request.password()) ? request.password() : "123456";
            user.setPasswordSalt(salt);
            user.setPasswordHash(PasswordHashUtil.sha256(rawPassword, salt));
        }
        saveOrUpdate(user);
        saveUserRoles(user.getId(), request.roleIds());
        cacheService.delete(TicketFlowCacheKeys.permissionUser(user.getId()));
        return user;
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            userRoleMapper.insert(relation);
        }
    }

    @Override
    public List<Long> findUserRoleIds(Long userId) {
        return userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser updateStatus(Long id, UserStatus status) {
        if (status == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "用户状态不能为空");
        }
        SysUser user = getRequired(id);
        user.setStatus(status);
        updateById(user);
        cacheService.delete(TicketFlowCacheKeys.permissionUser(user.getId()));
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser resetPassword(Long id, String password) {
        if (!StringUtils.hasText(password)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "新密码不能为空");
        }
        SysUser user = getRequired(id);
        String salt = UUID.randomUUID().toString().replace("-", "");
        user.setPasswordSalt(salt);
        user.setPasswordHash(PasswordHashUtil.sha256(password, salt));
        updateById(user);
        cacheService.delete(TicketFlowCacheKeys.permissionUser(user.getId()));
        return user;
    }

    @Override
    public List<UserOptionResponse> listUserOptions() {
        List<SysUser> users = list(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getStatus, UserStatus.ENABLED)
                .orderByAsc(SysUser::getDeptId)
                .orderByAsc(SysUser::getId));
        Map<Long, SysDept> deptMap = users.stream()
                .map(SysUser::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), deptIds -> deptIds.isEmpty()
                        ? Map.of()
                        : deptMapper.selectBatchIds(deptIds).stream()
                        .collect(Collectors.toMap(SysDept::getId, Function.identity()))));
        return users.stream()
                .map(user -> new UserOptionResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getRealName(),
                        user.getDeptId(),
                        deptMap.containsKey(user.getDeptId()) ? deptMap.get(user.getDeptId()).getDeptName() : null
                ))
                .toList();
    }

    private SysUser getRequired(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }
}
