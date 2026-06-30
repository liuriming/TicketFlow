package com.ticketflow.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.common.cache.RedisJsonCacheService;
import com.ticketflow.common.cache.TicketFlowCacheKeys;
import com.ticketflow.system.dto.SysMenuSaveRequest;
import com.ticketflow.system.entity.SysMenu;
import com.ticketflow.system.mapper.SysMenuMapper;
import com.ticketflow.system.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 菜单服务实现类。
 *
 * <p>负责维护目录、菜单和按钮权限。菜单数据既用于生成前端路由，
 * 也用于按钮和接口权限标识维护。</p>
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final RedisJsonCacheService cacheService;

    @Override
    public SysMenu saveMenu(Long id, SysMenuSaveRequest request) {
        SysMenu menu = id == null ? new SysMenu() : getById(id);
        if (menu == null) {
            menu = new SysMenu();
        }
        menu.setParentId(request.parentId() == null ? 0L : request.parentId());
        menu.setMenuName(request.menuName());
        menu.setType(request.type());
        menu.setPath(request.path());
        menu.setComponent(request.component());
        menu.setIcon(request.icon());
        menu.setPermission(request.permission());
        menu.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        menu.setVisible(request.visible() == null ? 1 : request.visible());
        saveOrUpdate(menu);
        cacheService.deleteByPattern(TicketFlowCacheKeys.permissionUserPattern());
        return menu;
    }
}
