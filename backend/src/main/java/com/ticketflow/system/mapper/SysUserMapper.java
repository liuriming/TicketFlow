package com.ticketflow.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketflow.system.entity.SysUser;

/**
 * 用户 Mapper。
 *
 * <p>负责用户表的基础增删改查，复杂查询由服务层组合条件。</p>
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
}
