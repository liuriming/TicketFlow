package com.ticketflow.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import com.ticketflow.system.enums.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体。
 *
 * <p>用户是系统登录主体，也是工单创建人、处理人和操作日志责任人的基础数据。
 * 密码字段只保存加盐摘要，不保存明文密码。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /**
     * 登录账号，全局唯一。
     */
    private String username;

    /**
     * 密码摘要，使用盐值和原始密码计算得到。
     */
    private String passwordHash;

    /**
     * 密码盐值，用于提高相同密码的摘要差异性。
     */
    private String passwordSalt;

    /**
     * 用户真实姓名。
     */
    private String realName;

    /**
     * 手机号，用于通知和人员查询。
     */
    private String phone;

    /**
     * 邮箱，用于通知和人员查询。
     */
    private String email;

    /**
     * 所属部门 ID。
     */
    private Long deptId;

    /**
     * 用户状态，禁用后不能登录。
     */
    private UserStatus status;
}
