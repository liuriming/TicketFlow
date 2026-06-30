package com.ticketflow.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketflow.system.dto.SysUserSaveRequest;
import com.ticketflow.system.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口。
 *
 * <p>封装用户资料维护、密码摘要和用户角色绑定逻辑。</p>
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 保存用户及其角色关系。
     *
     * @param id 用户 ID，传空表示新增。
     * @param request 用户保存请求。
     * @return 保存后的用户实体。
     */
    SysUser saveUser(Long id, SysUserSaveRequest request);

    /**
     * 查询用户绑定的角色 ID 集合。
     *
     * @param userId 用户 ID。
     * @return 角色 ID 集合。
     */
    List<Long> findUserRoleIds(Long userId);
}
