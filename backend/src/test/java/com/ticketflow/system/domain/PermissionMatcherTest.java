package com.ticketflow.system.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionMatcherTest {

    @Test
    void 没有配置接口权限时默认放行() {
        assertThat(PermissionMatcher.hasAnyPermission(List.of("ticket:list"))).isTrue();
    }

    @Test
    void 用户拥有任一接口权限时放行() {
        assertThat(PermissionMatcher.hasAnyPermission(List.of("ticket:list", "report:view"), "system:user:list", "report:view")).isTrue();
    }

    @Test
    void 用户缺少接口权限时拒绝() {
        assertThat(PermissionMatcher.hasAnyPermission(List.of("ticket:list"), "system:user:list")).isFalse();
    }
}
