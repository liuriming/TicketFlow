package com.ticketflow.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketflow.common.cache.RedisJsonCacheService;
import com.ticketflow.common.cache.TicketFlowCacheKeys;
import com.ticketflow.common.context.CurrentUserContext;
import com.ticketflow.common.context.LoginUser;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.common.util.PasswordHashUtil;
import com.ticketflow.system.dto.CurrentUserResponse;
import com.ticketflow.system.dto.LoginRequest;
import com.ticketflow.system.dto.LoginResponse;
import com.ticketflow.system.dto.MenuRouteResponse;
import com.ticketflow.system.entity.SysDept;
import com.ticketflow.system.entity.SysMenu;
import com.ticketflow.system.entity.SysRole;
import com.ticketflow.system.entity.SysRoleMenu;
import com.ticketflow.system.entity.SysUser;
import com.ticketflow.system.entity.SysUserRole;
import com.ticketflow.system.enums.DataScopeType;
import com.ticketflow.system.enums.MenuType;
import com.ticketflow.system.enums.UserStatus;
import com.ticketflow.system.mapper.SysDeptMapper;
import com.ticketflow.system.mapper.SysMenuMapper;
import com.ticketflow.system.mapper.SysRoleMapper;
import com.ticketflow.system.mapper.SysRoleMenuMapper;
import com.ticketflow.system.mapper.SysUserMapper;
import com.ticketflow.system.mapper.SysUserRoleMapper;
import com.ticketflow.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 认证服务实现类。
 *
 * <p>该实现采用“数据库保存用户与权限、Redis 保存登录 token”的方式。
 * 登录时校验密码摘要，生成随机 token 并写入 Redis；后续请求通过 token 反查用户，
 * 再装载角色、权限和数据范围。</p>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final long TOKEN_EXPIRE_SECONDS = 7 * 24 * 60 * 60L;
    private static final long PERMISSION_CACHE_SECONDS = 30 * 60L;

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysDeptMapper deptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final StringRedisTemplate redisTemplate;
    private final RedisJsonCacheService cacheService;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.username()));
        if (user == null || user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        String hashed = PasswordHashUtil.sha256(request.password(), user.getPasswordSalt());
        if (!Objects.equals(hashed, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(TicketFlowCacheKeys.loginToken(token), String.valueOf(user.getId()), Duration.ofSeconds(TOKEN_EXPIRE_SECONDS));
        return new LoginResponse(token, TOKEN_EXPIRE_SECONDS);
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (StringUtils.hasText(token)) {
            redisTemplate.delete(TicketFlowCacheKeys.loginToken(token));
        }
    }

    @Override
    public Optional<LoginUser> parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        String userIdText = redisTemplate.opsForValue().get(TicketFlowCacheKeys.loginToken(token));
        if (!StringUtils.hasText(userIdText)) {
            return Optional.empty();
        }
        SysUser user = userMapper.selectById(Long.valueOf(userIdText));
        if (user == null || user.getStatus() == UserStatus.DISABLED) {
            return Optional.empty();
        }
        String permissionCacheKey = TicketFlowCacheKeys.permissionUser(user.getId());
        Optional<LoginUser> cachedLoginUser = cacheService.get(permissionCacheKey, LoginUser.class);
        if (cachedLoginUser.isPresent()) {
            return cachedLoginUser;
        }
        LoginUser loginUser = buildLoginUser(user);
        cacheService.put(permissionCacheKey, loginUser, Duration.ofSeconds(PERMISSION_CACHE_SECONDS));
        return Optional.of(loginUser);
    }

    @Override
    public CurrentUserResponse currentUser() {
        LoginUser loginUser = CurrentUserContext.getRequired();
        SysDept dept = loginUser.deptId() == null ? null : deptMapper.selectById(loginUser.deptId());
        return new CurrentUserResponse(
                loginUser.userId(),
                loginUser.username(),
                loginUser.realName(),
                loginUser.deptId(),
                dept == null ? null : dept.getDeptName(),
                loginUser.dataScope(),
                loginUser.roles(),
                loginUser.permissions()
        );
    }

    @Override
    public List<MenuRouteResponse> currentRoutes() {
        LoginUser loginUser = CurrentUserContext.getRequired();
        List<SysMenu> menus = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                .ne(SysMenu::getType, MenuType.BUTTON)
                .eq(SysMenu::getVisible, 1)
                .orderByAsc(SysMenu::getSortOrder));
        if (!loginUser.roles().contains("ADMIN")) {
            Set<String> allowedPermissions = new HashSet<>(loginUser.permissions());
            menus = menus.stream()
                    .filter(menu -> !StringUtils.hasText(menu.getPermission()) || allowedPermissions.contains(menu.getPermission()))
                    .toList();
        }
        return buildRouteTree(menus);
    }

    private LoginUser buildLoginUser(SysUser user) {
        SysDept dept = user.getDeptId() == null ? null : deptMapper.selectById(user.getDeptId());
        List<SysRole> roles = findRoles(user.getId());
        List<SysMenu> menus = findMenus(roles);
        DataScopeType dataScope = roles.stream()
                .map(SysRole::getDataScope)
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(this::dataScopeRank))
                .orElse(DataScopeType.SELF);
        return new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getDeptId(),
                dept == null ? null : dept.getPath(),
                dataScope,
                roles.stream().map(SysRole::getRoleCode).toList(),
                menus.stream()
                        .map(SysMenu::getPermission)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .toList()
        );
    }

    private List<SysRole> findRoles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectByIds(roleIds).stream()
                .filter(role -> Objects.equals(role.getEnabled(), 1))
                .toList();
    }

    private List<SysMenu> findMenus(List<SysRole> roles) {
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = roleMenuMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery()
                        .in(SysRoleMenu::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return menuMapper.selectByIds(menuIds);
    }

    private int dataScopeRank(DataScopeType dataScopeType) {
        return switch (dataScopeType) {
            case SELF -> 1;
            case DEPT -> 2;
            case DEPT_AND_CHILD -> 3;
            case ALL -> 4;
        };
    }

    private List<MenuRouteResponse> buildRouteTree(List<SysMenu> menus) {
        Map<Long, SysMenu> menuMap = menus.stream().collect(Collectors.toMap(SysMenu::getId, Function.identity()));
        Map<Long, List<SysMenu>> childrenMap = menus.stream()
                .collect(Collectors.groupingBy(menu -> Optional.ofNullable(menu.getParentId()).orElse(0L)));
        List<SysMenu> roots = menus.stream()
                .filter(menu -> Optional.ofNullable(menu.getParentId()).orElse(0L) == 0L
                        || !menuMap.containsKey(menu.getParentId()))
                .sorted(Comparator.comparing(SysMenu::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        List<MenuRouteResponse> routes = new ArrayList<>();
        for (SysMenu root : roots) {
            routes.add(toRoute(root, childrenMap));
        }
        return routes;
    }

    private MenuRouteResponse toRoute(SysMenu menu, Map<Long, List<SysMenu>> childrenMap) {
        List<MenuRouteResponse> children = childrenMap.getOrDefault(menu.getId(), List.of()).stream()
                .sorted(Comparator.comparing(SysMenu::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(child -> toRoute(child, childrenMap))
                .toList();
        return new MenuRouteResponse(
                menu.getId(),
                menu.getParentId(),
                menu.getMenuName(),
                menu.getPath(),
                menu.getComponent(),
                menu.getIcon(),
                menu.getPermission(),
                children
        );
    }
}
