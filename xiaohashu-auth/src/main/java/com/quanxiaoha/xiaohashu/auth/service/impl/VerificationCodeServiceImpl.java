package com.quanxiaoha.xiaohashu.auth.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.auth.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.auth.model.vo.verificationcod.SendVerificationCodeReqVO;
import com.quanxiaoha.xiaohashu.auth.service.VerificationCodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.quanxiaoha.xiaohashu.auth.enums.ResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY;

@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 发送验证码
     * @param sendVerificationCodeReqVO
     * @return
     */
    @Override
    public Response send(SendVerificationCodeReqVO sendVerificationCodeReqVO) {

        String phoneNumber = sendVerificationCodeReqVO.getPhone();

        // 构建redis key
        String key = RedisKeyConstants.buildVerificationCodeKey(phoneNumber);

        //生成验证码
        String code = RandomUtil.randomNumbers(6);

        //判断key值是否存在
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key,code,3*60, TimeUnit.SECONDS);

        //已存在 提示用户请求验证过于频繁
        if(BooleanUtil.isFalse(flag)){
            return Response.fail(VERIFICATION_CODE_SEND_FREQUENTLY);
        };

        //不存在
        //ZJY TODO 2025/10/12:后续将调用第三方短信发送服务
        log.info("==> 手机号: {}, 已发送验证码：【{}】", phoneNumber, code);

        return Response.success();
    }
}
