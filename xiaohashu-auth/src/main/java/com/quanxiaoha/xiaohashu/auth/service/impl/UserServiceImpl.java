package com.quanxiaoha.xiaohashu.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.BooleanUtil;
import com.google.common.collect.Lists;
import com.quanxiaoha.framework.common.eumns.DeletedEnum;
import com.quanxiaoha.framework.common.eumns.StatusEnum;
import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.framework.common.util.JsonUtils;
import com.quanxiaoha.xiaohashu.auth.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.auth.constant.RoleConstants;
import com.quanxiaoha.xiaohashu.auth.domain.dataobject.UserDO;
import com.quanxiaoha.xiaohashu.auth.domain.dataobject.UserRoleDO;
import com.quanxiaoha.xiaohashu.auth.domain.mapper.UserDOMapper;
import com.quanxiaoha.xiaohashu.auth.domain.mapper.UserRoleDOMapper;
import com.quanxiaoha.xiaohashu.auth.enums.LoginTypeEnum;
import com.quanxiaoha.xiaohashu.auth.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.auth.model.vo.user.UserLoginReqVO;
import com.quanxiaoha.xiaohashu.auth.service.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static com.quanxiaoha.xiaohashu.auth.enums.LoginTypeEnum.VERIFICATION_CODE;
import static com.quanxiaoha.xiaohashu.auth.enums.ResponseCodeEnum.VERIFICATION_CODE_ERROR;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserRoleDOMapper userRoleDOMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 登录与注册
     * @param userLoginReqVO
     * @return
     */
    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {

        //判断登录类型
        Integer type = userLoginReqVO.getType();
        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);

        //获取手机号
        String phone = userLoginReqVO.getPhone();
        //获取验证码
        String code = userLoginReqVO.getCode();

        Long userId = null;

        switch (loginTypeEnum) {

            //手机号登录
            case VERIFICATION_CODE:

                // 校验入参验证码是否为空
                if (StringUtils.isBlank(code)) {
                    return Response.fail(ResponseCodeEnum.PARAM_NOT_VALID.getErrorCode(), "验证码不能为空");
                }

            //判断验证码是否正确
            String key = RedisKeyConstants.buildVerificationCodeKey(phone);

            Boolean flag = redisTemplate.hasKey(key);

            if (BooleanUtil.isFalse(flag)) {
                throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
            };

            //判断验证码是否相同
            String codeN = (String) redisTemplate.opsForValue().get(key);
            if (!code.equals(codeN)) {
                throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
            };

            UserDO userDO = userDOMapper.selectByPhone(phone);

            log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO));

            if (userDO != null) {
                //ZJY TODO 2025/10/13:已注册
                userId = userDO.getId();

            }
            else{
                //ZJY TODO 2025/10/13:未注册
                // 若此用户还没有注册，系统自动注册该用户
                /**
                 * userId = registerUser(phone);
                 * 自调用会导致事务失效！！！
                 */

                userId = registerUser(phone);
            };

            break;

            case PASSWORD:
            //ZJY TODO 2025/10/13:密码登录

                break;

            default:
                break;
        }

        // SaToken 登录用户, 入参为用户 ID
        StpUtil.login(userId);

        // 获取 Token 令牌
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 返回 Token 令牌
        return Response.success(tokenInfo.tokenValue);
    }

    /**
     * 系统自动注册用户
     * @param phone
     * @return
     */

    //声明式事务
//    @Transactional(rollbackFor = Exception.class)
//    public Long registerUser(String phone) {
//        // 获取全局自增的小哈书 ID
//        Long xiaohashuId = redisTemplate.opsForValue().increment(RedisKeyConstants.XIAOHASHU_ID_GENERATOR_KEY);
//
//        UserDO userDO = UserDO.builder()
//                .phone(phone)
//                .xiaohashuId(String.valueOf(xiaohashuId)) // 自动生成小红书号 ID
//                .nickname("小红薯" + xiaohashuId) // 自动生成昵称, 如：小红薯10000
//                .status(StatusEnum.ENABLE.getValue()) // 状态为启用
//                .createTime(LocalDateTime.now())
//                .updateTime(LocalDateTime.now())
//                .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
//                .build();
//
//        // 添加入库
//        userDOMapper.insert(userDO);
//
//        // 获取刚刚添加入库的用户 ID
//        Long userId = userDO.getId();
//
//        // 给该用户分配一个默认角色
//        UserRoleDO userRoleDO = UserRoleDO.builder()
//                .userId(userId)
//                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
//                .createTime(LocalDateTime.now())
//                .updateTime(LocalDateTime.now())
//                .isDeleted(DeletedEnum.NO.getValue())
//                .build();
//        userRoleDOMapper.insert(userRoleDO);
//
//        // 将该用户的角色 ID 存入 Redis 中
//        List<Long> roles = Lists.newArrayList();
//        roles.add(RoleConstants.COMMON_USER_ROLE_ID);
//        String userRolesKey = RedisKeyConstants.buildUserRoleKey(phone);
//        redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));
//
//        return userId;
//    }

    private Long registerUser(String phone) {

        return transactionTemplate.execute(status -> {
            try {
                // 获取全局自增的小哈书 ID
                Long xiaohashuId = redisTemplate.opsForValue().increment(RedisKeyConstants.XIAOHASHU_ID_GENERATOR_KEY);

                UserDO userDO = UserDO.builder()
                        .phone(phone)
                        .xiaohashuId(String.valueOf(xiaohashuId)) // 自动生成小红书号 ID
                        .nickname("小红薯" + xiaohashuId) // 自动生成昵称, 如：小红薯10000
                        .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                        .build();

                // 添加入库
                userDOMapper.insert(userDO);


                // 获取刚刚添加入库的用户 ID
                Long userId = userDO.getId();

                // 给该用户分配一个默认角色
                UserRoleDO userRoleDO = UserRoleDO.builder()
                        .userId(userId)
                        .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(DeletedEnum.NO.getValue())
                        .build();
                userRoleDOMapper.insert(userRoleDO);

                // 将该用户的角色 ID 存入 Redis 中
                List<Long> roles = Lists.newArrayList();
                roles.add(RoleConstants.COMMON_USER_ROLE_ID);
                String userRolesKey = RedisKeyConstants.buildUserRoleKey(phone);
                redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

                return userId;
            } catch (Exception e) {
                status.setRollbackOnly(); // 标记事务为回滚
                log.error("==> 系统注册用户异常: ", e);
                return null;
            }
        });
    }

}
