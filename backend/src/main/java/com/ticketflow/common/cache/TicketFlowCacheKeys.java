package com.ticketflow.common.cache;

/**
 * TicketFlow Redis 缓存 Key 约定。
 *
 * <p>集中管理登录 token、权限缓存、字典缓存和热点统计缓存前缀，避免不同模块手写字符串导致
 * Key 不一致或清理范围不准确。</p>
 */
public final class TicketFlowCacheKeys {

    private static final String PREFIX = "ticketflow:";
    private static final String LOGIN_PREFIX = PREFIX + "login:";
    private static final String PERMISSION_USER_PREFIX = PREFIX + "permission:user:";
    private static final String DICT_PREFIX = PREFIX + "dict:";
    private static final String STATS_PREFIX = PREFIX + "stats:";

    private TicketFlowCacheKeys() {
    }

    /**
     * 登录 token 缓存 Key。
     *
     * @param token 登录 token。
     * @return token 缓存 Key。
     */
    public static String loginToken(String token) {
        return LOGIN_PREFIX + token;
    }

    /**
     * 当前用户权限快照缓存 Key。
     *
     * @param userId 用户 ID。
     * @return 权限缓存 Key。
     */
    public static String permissionUser(Long userId) {
        return PERMISSION_USER_PREFIX + userId;
    }

    /**
     * 权限快照缓存清理 Pattern。
     *
     * @return 权限缓存 Pattern。
     */
    public static String permissionUserPattern() {
        return PERMISSION_USER_PREFIX + "*";
    }

    /**
     * 字典缓存 Key。
     *
     * @param name 字典名称。
     * @return 字典缓存 Key。
     */
    public static String dictionary(String name) {
        return DICT_PREFIX + name;
    }

    /**
     * 字典缓存清理 Pattern。
     *
     * @return 字典缓存 Pattern。
     */
    public static String dictionaryPattern() {
        return DICT_PREFIX + "*";
    }

    /**
     * 热点统计缓存 Key。
     *
     * @param name 统计名称。
     * @return 热点统计缓存 Key。
     */
    public static String hotStats(String name) {
        return STATS_PREFIX + name;
    }

    /**
     * 热点统计缓存清理 Pattern。
     *
     * @return 热点统计缓存 Pattern。
     */
    public static String hotStatsPattern() {
        return STATS_PREFIX + "*";
    }
}
