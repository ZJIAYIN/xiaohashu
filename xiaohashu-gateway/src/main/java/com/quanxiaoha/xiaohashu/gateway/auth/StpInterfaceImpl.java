package com.quanxiaoha.xiaohashu.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.quanxiaoha.xiaohashu.gateway.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: 犬小哈
 * @date: 2024/4/5 18:04
 * @version: v1.0.0
 * @description: 自定义权限验证接口扩展
 **/
/**
 * 自定义权限验证接口扩展
 */
@Component
@Slf4j
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {

        // 返回此 loginId 拥有的权限列表

        //先根据id构建key找出该用户对应的角色
        String userRolesKey = RedisKeyConstants.buildUserRoleKey(Long.valueOf(loginId.toString()));
        String useRolesValue = redisTemplate.opsForValue().get(userRolesKey);

        if(StringUtils.isBlank(useRolesValue)){
            return null;
        };

        // 将 JSON 字符串转换为 List<String> 角色集合
        List<String> userRoleKeys = objectMapper.readValue(useRolesValue, new TypeReference<>() {});

        //再根据角色 来构建 key 找出该角色对应的所有权限
        if(CollUtil.isNotEmpty(userRoleKeys)){
            //判断是否为空
            //构造 角色-权限key
            List<String> rolePermissionsKeys = userRoleKeys.stream().map(userRoleKey -> RedisKeyConstants.buildRolePermissionsKey(userRoleKey)).collect(Collectors.toList());

            // 通过 key 集合批量查询权限，提升查询性能。
            List<String> rolePermissionsValues = redisTemplate.opsForValue().multiGet(rolePermissionsKeys);

            if (CollUtil.isNotEmpty(rolePermissionsValues)) {
                List<String> permissions = Lists.newArrayList();

                // 遍历所有角色的权限集合，统一添加到 permissions 集合中
                rolePermissionsValues.forEach(jsonValue -> {
                    try {
                        // 将 JSON 字符串转换为 List<String> 权限集合
                        List<String> rolePermissions = objectMapper.readValue(jsonValue, new TypeReference<>() {});
                        permissions.addAll(rolePermissions);
                    } catch (JsonProcessingException e) {
                        log.error("==> JSON 解析错误: ", e);
                    }
                });

                // 返回此用户所拥有的权限
                return permissions;
            }
        };

        //返回
        return null;
    }

    @SneakyThrows
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 返回此 loginId 拥有的角色列表

        // 构建 用户-角色 Redis Key
        String userRolesKey = RedisKeyConstants.buildUserRoleKey(Long.valueOf(loginId.toString()));

        // 根据用户 ID ，从 Redis 中获取该用户的角色集合
        String useRolesValue = redisTemplate.opsForValue().get(userRolesKey);

        if (StringUtils.isBlank(useRolesValue)) {
            return null;
        }

        // 将 JSON 字符串转换为 List<String> 集合
        return objectMapper.readValue(useRolesValue, new TypeReference<>() {});
    }

}
