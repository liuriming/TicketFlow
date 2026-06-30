package com.ticketflow.common.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查接口。
 *
 * <p>用于 Docker Compose、反向代理和本地调试快速确认后端服务是否启动。</p>
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResult<Map<String, String>> health() {
        return ApiResult.success(Map.of("status", "UP"));
    }
}
