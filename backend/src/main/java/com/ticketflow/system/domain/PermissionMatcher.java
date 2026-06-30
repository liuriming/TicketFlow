package com.ticketflow.system.domain;

import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限匹配器。
 *
 * <p>用于判断当前用户权限集合是否满足接口要求。该类不依赖 Spring 和数据库，便于在拦截器、
 * 单元测试或后续按钮级权限判断中复用。</p>
 */
public final class PermissionMatcher {

    private PermissionMatcher() {
    }

    /**
     * 判断当前权限集合是否包含任意一个接口要求权限。
     *
     * @param ownedPermissions 当前用户拥有的权限标识。
     * @param requiredPermissions 接口要求的权限标识。
     * @return 不要求权限或命中任一权限时返回 true，否则返回 false。
     */
    public static boolean hasAnyPermission(Collection<String> ownedPermissions, String... requiredPermissions) {
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return true;
        }
        if (CollectionUtils.isEmpty(ownedPermissions)) {
            return false;
        }
        Set<String> owned = ownedPermissions.stream()
                .filter(permission -> permission != null && !permission.isBlank())
                .collect(Collectors.toSet());
        return Arrays.stream(requiredPermissions)
                .filter(permission -> permission != null && !permission.isBlank())
                .anyMatch(owned::contains);
    }
}
