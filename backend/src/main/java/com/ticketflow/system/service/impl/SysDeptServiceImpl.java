package com.ticketflow.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.system.dto.SysDeptSaveRequest;
import com.ticketflow.system.entity.SysDept;
import com.ticketflow.system.mapper.SysDeptMapper;
import com.ticketflow.system.service.SysDeptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门服务实现类。
 *
 * <p>负责维护部门基本信息，并根据父部门自动计算 path。
 * path 是后续“本部门及下级”数据权限判断的核心字段。</p>
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDept saveDept(Long id, SysDeptSaveRequest request) {
        SysDept dept = id == null ? new SysDept() : getById(id);
        if (dept == null) {
            dept = new SysDept();
        }
        Long parentId = request.parentId() == null ? 0L : request.parentId();
        dept.setParentId(parentId);
        dept.setDeptName(request.deptName());
        dept.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        dept.setEnabled(1);
        saveOrUpdate(dept);

        if (parentId == 0L) {
            dept.setPath(String.valueOf(dept.getId()));
        } else {
            SysDept parent = getById(parentId);
            dept.setPath(parent == null ? String.valueOf(dept.getId()) : parent.getPath() + "/" + dept.getId());
        }
        updateById(dept);
        return dept;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDept updateEnabled(Long id, Integer enabled) {
        SysDept dept = getById(id);
        if (dept == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "部门不存在");
        }
        dept.setEnabled(normalizeEnabled(enabled));
        updateById(dept);
        return dept;
    }

    private Integer normalizeEnabled(Integer enabled) {
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "启用状态只能是 0 或 1");
        }
        return enabled;
    }
}
