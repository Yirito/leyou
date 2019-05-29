package com.leyou.auth.web;

import com.leyou.auth.service.AuthService;
import com.leyou.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @Value("${ly.jwt.cookieName}")//另一种获取自定义属性
    private String cookieName;

    /**
     * 登陆授权
     *
     * @param username
     * @param password
     * @return 不需要返回值，把token写在cookie就行
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletResponse response,
            HttpServletRequest request) {
        //登陆
        String token = authService.login(username, password);
        // 将token写入cookie,并指定httpOnly，防止通过JS获取和修改
        CookieUtils.newBuilder(response)
                .httpOnly().build(cookieName, token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
