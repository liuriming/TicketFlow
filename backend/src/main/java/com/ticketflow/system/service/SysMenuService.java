package com.ticketflow.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketflow.system.dto.SysMenuSaveRequest;
import com.ticketflow.system.entity.SysMenu;

/**
 * 菜单服务接口。
 *
 * <p>封装菜单、路由和按钮权限维护逻辑。</p>
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 保存菜单。
     *
     * @param id 菜单 ID，传空表示新增。
     * @param request 菜单保存请求。
     * @return 保存后的菜单实体。
     */
    SysMenu saveMenu(Long id, SysMenuSaveRequest request);

    /**
     * 更新菜单或按钮权限启用状态。
     *
     * @param id 菜单 ID。
     * @param enabled 启用状态：1 启用，0 停用。
     * @return 更新后的菜单实体。
     */
    SysMenu updateEnabled(Long id, Integer enabled);
}
