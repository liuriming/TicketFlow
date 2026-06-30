package com.ticketflow.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketflow.system.dto.SysRoleSaveRequest;
import com.ticketflow.system.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口。
 *
 * <p>封装角色基础信息维护、数据权限配置和角色菜单授权。</p>
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 保存角色及其菜单关系。
     *
     * @param id 角色 ID，传空表示新增。
     * @param request 角色保存请求。
     * @return 保存后的角色实体。
     */
    SysRole saveRole(Long id, SysRoleSaveRequest request);

    /**
     * 查询角色授权的菜单 ID 集合。
     *
     * @param roleId 角色 ID。
     * @return 菜单 ID 集合。
     */
    List<Long> findRoleMenuIds(Long roleId);
}
