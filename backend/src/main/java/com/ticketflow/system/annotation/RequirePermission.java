package com.ticketflow.system.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口权限要求注解。
 *
 * <p>用于标记控制器或控制器方法需要的权限标识。方法上的注解优先级高于类上的注解；
 * 如果配置多个权限，当前用户拥有任意一个即可访问。</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 访问当前接口需要的权限标识集合。
     *
     * @return 权限标识数组。
     */
    String[] value();
}
