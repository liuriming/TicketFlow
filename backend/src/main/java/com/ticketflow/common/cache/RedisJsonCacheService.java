package com.ticketflow.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Redis JSON 缓存服务。
 *
 * <p>该组件基于 {@link StringRedisTemplate} 统一处理对象序列化、反序列化、TTL 写入和按前缀清理。
 * 业务模块只需要传入明确的缓存 Key 和类型，避免在各个服务实现类中重复编写 JSON 处理代码。</p>
 */
@Component
@RequiredArgsConstructor
public class RedisJsonCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 读取指定类型的缓存对象。
     *
     * @param key 缓存 Key。
     * @param type 目标类型。
     * @param <T> 目标类型泛型。
     * @return 命中时返回对象，否则返回空。
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        String value = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, type));
        } catch (JsonProcessingException exception) {
            delete(key);
            return Optional.empty();
        }
    }

    /**
     * 读取带泛型结构的缓存对象。
     *
     * @param key 缓存 Key。
     * @param typeReference 目标类型引用。
     * @param <T> 目标类型泛型。
     * @return 命中时返回对象，否则返回空。
     */
    public <T> Optional<T> get(String key, TypeReference<T> typeReference) {
        String value = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, typeReference));
        } catch (JsonProcessingException exception) {
            delete(key);
            return Optional.empty();
        }
    }

    /**
     * 写入带过期时间的缓存对象。
     *
     * @param key 缓存 Key。
     * @param value 待缓存对象。
     * @param ttl 过期时间。
     */
    public void put(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("缓存对象序列化失败", exception);
        }
    }

    /**
     * 删除指定缓存 Key。
     *
     * @param key 缓存 Key。
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 按 Redis Pattern 删除缓存 Key。
     *
     * @param pattern 缓存 Key Pattern。
     */
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        redisTemplate.delete(keys);
    }
}
