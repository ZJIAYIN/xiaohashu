package com.quanxiaoha.xiaohashu.auth.service;


import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.auth.model.vo.verificationcod.SendVerificationCodeReqVO;

public interface VerificationCodeService {

    /**
     * 发送验证码
     * @param sendVerificationCodeReqVO
     * @return
     */
    Response send(SendVerificationCodeReqVO sendVerificationCodeReqVO);
}
