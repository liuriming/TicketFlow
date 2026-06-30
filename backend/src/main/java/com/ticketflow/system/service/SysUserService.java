package com.ticketflow.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketflow.system.dto.SysUserSaveRequest;
import com.ticketflow.system.dto.UserOptionResponse;
import com.ticketflow.system.entity.SysUser;
import com.ticketflow.system.enums.UserStatus;

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

    /**
     * 更新用户启用状态。
     *
     * @param id 用户 ID。
     * @param status 新状态。
     * @return 更新后的用户实体。
     */
    SysUser updateStatus(Long id, UserStatus status);

    /**
     * 重置用户登录密码。
     *
     * @param id 用户 ID。
     * @param password 新密码。
     * @return 更新后的用户实体。
     */
    SysUser resetPassword(Long id, String password);

    /**
     * 查询启用用户下拉选项。
     *
     * <p>用于工单转派、派单规则配置等页面选择处理人，返回部门名称避免前端再次拼装。</p>
     *
     * @return 用户选项列表。
     */
    List<UserOptionResponse> listUserOptions();
}
