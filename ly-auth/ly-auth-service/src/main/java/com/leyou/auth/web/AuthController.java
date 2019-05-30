package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    //@Value("${ly.jwt.cookieName}")//另一种获取自定义属性,无须@EnableConfigurationProperties(JwtProperties.class)。太麻烦
    //private String cookieName;

    /**
     * 用户授权
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
                .httpOnly().request(request).build(prop.getCookieName(), token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 用户鉴权
     * 校验用户登陆状态
     *
     * @return
     * @CookieValue 取出cookie
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue("LY_TOKEN") String token,
            HttpServletResponse response,
            HttpServletRequest request) {
        try {
            //解析token
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());

            //刷新token，防止过期
            String newToken = JwtUtils.generateToken(info, prop.getPrivateKey(), prop.getExpire());

            //写入cookie
            CookieUtils.newBuilder(response)
                    .httpOnly().request(request).build(prop.getCookieName(), newToken);

            //已登录，返回用户信息
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            //token已过去，或者token被篡改
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }
}
