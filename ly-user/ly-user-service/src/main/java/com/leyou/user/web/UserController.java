package com.leyou.user.web;

import com.leyou.user.pojo.User;
import com.leyou.user.sercvice.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 校验数据（校验用户数据是否唯一）
     *
     * @param data
     * @param type 1用户名,2手机号
     * @return 返回布尔值
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data, @PathVariable("type") Integer type) {
        return ResponseEntity.ok(userService.checkData(data, type));
    }

    /**
     * 发送短信
     *
     * @param phone 手机号
     * @return 无返回值
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone) {
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 注册
     *
     * @param user 用户信息
     * @param code 验证码
     * @return 无返回值
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult result, @RequestParam("code") String code) {
//        if (result.hasFieldErrors()) {
//            throw new RuntimeException(result.getFieldErrors().stream()
//                    .map(e -> e.getDefaultMessage()).collect(Collectors.joining("|")));
//        }
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     *
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    public ResponseEntity<User> queryUserByUserNameAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        return ResponseEntity.ok(userService.queryUserByUserNameAndPassword(username, password));
    }
}
