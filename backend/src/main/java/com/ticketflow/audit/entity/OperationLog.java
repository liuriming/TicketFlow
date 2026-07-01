package com.ticketflow.audit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作审计日志实体。
 *
 * <p>记录变更类接口的操作人、请求方法、请求地址、客户端 IP、执行结果和耗时。
 * 该日志用于问题追踪和管理审计，不承载业务状态流转。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_log")
public class OperationLog extends BaseEntity {

    /**
     * 操作人用户 ID，未解析到登录态时为空。
     */
    private Long operatorId;

    /**
     * 操作人展示名称，优先使用真实姓名。
     */
    private String operatorName;

    /**
     * HTTP 请求方法，例如 POST、PUT、DELETE。
     */
    private String requestMethod;

    /**
     * 请求路径，不包含域名。
     */
    private String requestUri;

    /**
     * 查询字符串，可能为空。
     */
    private String queryString;

    /**
     * 客户端 IP 地址。
     */
    private String clientIp;

    /**
     * 是否执行成功：1 成功，0 失败。
     */
    private Integer success;

    /**
     * 异常消息，成功时为空。
     */
    private String errorMessage;

    /**
     * 请求处理耗时，单位毫秒。
     */
    private Long durationMs;
}
