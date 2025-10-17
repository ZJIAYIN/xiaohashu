package com.quanxiaoha.xiaohashu.auth.controller;

import com.quanxiaoha.framework.biz.operationlog.aspect.ApiOperationLog;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.auth.model.vo.user.UpdatePasswordReqVO;
import com.quanxiaoha.xiaohashu.auth.model.vo.user.UserLoginReqVO;
import com.quanxiaoha.xiaohashu.auth.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/login")
    @ResponseBody
    @ApiOperationLog(description = "用户登录/注册")
    public Response<String> login(@RequestBody @Validated UserLoginReqVO userLoginReqVO){
        return userService.loginAndRegister(userLoginReqVO);
    };

    @PostMapping("/logout")
    @ApiOperationLog(description = "账号登出")
    @ResponseBody
    public Response<?> logout() {

        // todo 账号退出登录逻辑待实现
        userService.logout();
        return Response.success();
    }

    @PostMapping("/password/update")
    @ApiOperationLog(description = "修改密码")
    @ResponseBody
    public Response<?> updatePassword(@Validated @RequestBody UpdatePasswordReqVO updatePasswordReqVO) {

        userService.updatePassword(updatePasswordReqVO);
        return Response.success();
    }



}
