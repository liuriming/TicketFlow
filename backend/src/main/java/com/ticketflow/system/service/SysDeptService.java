package com.ticketflow.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketflow.system.dto.SysDeptSaveRequest;
import com.ticketflow.system.entity.SysDept;

/**
 * 部门服务接口。
 *
 * <p>封装部门维护和部门层级路径计算逻辑。</p>
 */
public interface SysDeptService extends IService<SysDept> {

    /**
     * 保存部门并计算部门路径。
     *
     * @param id 部门 ID，传空表示新增。
     * @param request 部门保存请求。
     * @return 保存后的部门实体。
     */
    SysDept saveDept(Long id, SysDeptSaveRequest request);

    /**
     * 更新部门启用状态。
     *
     * @param id 部门 ID。
     * @param enabled 启用状态：1 启用，0 停用。
     * @return 更新后的部门实体。
     */
    SysDept updateEnabled(Long id, Integer enabled);
}
