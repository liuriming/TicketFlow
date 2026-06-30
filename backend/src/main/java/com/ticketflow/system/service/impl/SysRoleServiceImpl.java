package com.ticketflow.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.common.cache.RedisJsonCacheService;
import com.ticketflow.common.cache.TicketFlowCacheKeys;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.system.dto.SysRoleSaveRequest;
import com.ticketflow.system.entity.SysRole;
import com.ticketflow.system.entity.SysRoleMenu;
import com.ticketflow.system.mapper.SysRoleMapper;
import com.ticketflow.system.mapper.SysRoleMenuMapper;
import com.ticketflow.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现类。
 *
 * <p>负责维护角色基础信息、数据权限范围和角色菜单授权关系。
 * 数据权限在用户登录时会从角色中合并计算。</p>
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper roleMenuMapper;
    private final RedisJsonCacheService cacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole saveRole(Long id, SysRoleSaveRequest request) {
        SysRole role = id == null ? new SysRole() : getById(id);
        if (role == null) {
            role = new SysRole();
        }
        role.setRoleName(request.roleName());
        role.setRoleCode(request.roleCode());
        role.setDataScope(request.dataScope());
        role.setEnabled(1);
        role.setSortOrder(0);
        saveOrUpdate(role);
        saveRoleMenus(role.getId(), request.menuIds());
        cacheService.deleteByPattern(TicketFlowCacheKeys.permissionUserPattern());
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole updateEnabled(Long id, Integer enabled) {
        SysRole role = getById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        role.setEnabled(normalizeEnabled(enabled));
        updateById(role);
        cacheService.deleteByPattern(TicketFlowCacheKeys.permissionUserPattern());
        return role;
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            roleMenuMapper.insert(relation);
        }
    }

    @Override
    public List<Long> findRoleMenuIds(Long roleId) {
        return roleMenuMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery()
                        .eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList();
    }

    private Integer normalizeEnabled(Integer enabled) {
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "启用状态只能是 0 或 1");
        }
        return enabled;
    }
}
