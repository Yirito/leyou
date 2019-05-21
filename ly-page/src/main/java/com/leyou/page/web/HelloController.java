package com.leyou.page.web;

import com.leyou.page.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
public class HelloController {

    @GetMapping("hello")
    public String toHello(Model model) {
        //model.addAttribute("msg","hello,thymeleaf!");//这个查看hello.html文件
        User user = new User();
        user.setAge(21);
        user.setName("Jack Chen");
        user.setFriend(new User("李小龙", 30, null));
        User user2 = new User("李小龙", 30, null);
        model.addAttribute("users", Arrays.asList(user,user2));
        /**
         * 普通字符串被当成识图名称，结合前缀和后缀寻找视图
         * 查看ThymeleafAutoConfiguration源码发现，自动配置，会在java或资源目录下的templates找到hello.html文件
         * 所以可以提前创建好templates文件夹和hello.html文件（默认配置）
         */
        return "hello";
    }
}
