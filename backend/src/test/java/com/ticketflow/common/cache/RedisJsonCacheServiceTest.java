package com.ticketflow.common.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisJsonCacheServiceTest {

    @Test
    void 可以从Redis读取Json并反序列化为指定类型() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mockValueOperations(redisTemplate);
        when(valueOperations.get("ticketflow:dict:demo")).thenReturn("[\"网络故障\",\"账号权限\"]");
        RedisJsonCacheService cacheService = new RedisJsonCacheService(redisTemplate, new ObjectMapper());

        Optional<List<String>> result = cacheService.get("ticketflow:dict:demo", new TypeReference<>() {
        });

        assertThat(result).contains(List.of("网络故障", "账号权限"));
    }

    @Test
    void 写入缓存时会保存Json并设置过期时间() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mockValueOperations(redisTemplate);
        RedisJsonCacheService cacheService = new RedisJsonCacheService(redisTemplate, new ObjectMapper());

        cacheService.put("ticketflow:stats:demo", List.of("统计"), Duration.ofMinutes(5));

        verify(valueOperations).set(eq("ticketflow:stats:demo"), eq("[\"统计\"]"), eq(Duration.ofMinutes(5)));
    }

    @Test
    void 可以按Pattern清理缓存Key() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(redisTemplate.keys("ticketflow:permission:user:*")).thenReturn(Set.of("ticketflow:permission:user:1"));
        RedisJsonCacheService cacheService = new RedisJsonCacheService(redisTemplate, new ObjectMapper());

        cacheService.deleteByPattern("ticketflow:permission:user:*");

        verify(redisTemplate).delete(Set.of("ticketflow:permission:user:1"));
    }

    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> mockValueOperations(StringRedisTemplate redisTemplate) {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        return valueOperations;
    }
}
