package com.ticketflow.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 密码摘要工具。
 *
 * <p>第一版使用 SHA-256 加盐摘要，满足演示项目的密码校验需求。
 * 生产环境建议替换为 BCrypt、Argon2 等带自适应成本的密码哈希算法。</p>
 */
public final class PasswordHashUtil {

    private PasswordHashUtil() {
    }

    public static String sha256(String rawPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((salt + ":" + rawPassword).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", exception);
        }
    }
}
