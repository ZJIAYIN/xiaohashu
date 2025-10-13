package com.quanxiaoha.xiaohashu.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.BooleanUtil;
import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.auth.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.auth.domain.dataobject.UserDO;
import com.quanxiaoha.xiaohashu.auth.domain.mapper.UserDOMapper;
import com.quanxiaoha.xiaohashu.auth.enums.LoginTypeEnum;
import com.quanxiaoha.xiaohashu.auth.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.auth.model.vo.user.UserLoginReqVO;
import com.quanxiaoha.xiaohashu.auth.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.quanxiaoha.xiaohashu.auth.enums.LoginTypeEnum.VERIFICATION_CODE;
import static com.quanxiaoha.xiaohashu.auth.enums.ResponseCodeEnum.VERIFICATION_CODE_ERROR;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Resource
    private RedisTemplate redisTemplate;

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

        switch (loginTypeEnum) {

            //手机号登录
            case VERIFICATION_CODE:
            //获取手机号
            String phone = userLoginReqVO.getPhone();
            //获取验证码
            String code = userLoginReqVO.getCode();

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

            if (userDO != null) {
                //ZJY TODO 2025/10/13:已注册
                Long id = userDO.getId();

            }
            else{
                //ZJY TODO 2025/10/13:未注册
            };

            case PASSWORD:
            //ZJY TODO 2025/10/13:密码登录
        }

        return null;
    }

}
