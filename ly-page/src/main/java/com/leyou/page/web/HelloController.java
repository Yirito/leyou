package com.leyou.page.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("hello")
    public String toHello(Model model) {
        model.addAttribute("msg","hello,thymeleaf!");//这个查看hello.html文件
        /**
         * 普通字符串被当成识图名称，结合前缀和后缀寻找视图
         * 查看ThymeleafAutoConfiguration源码发现，自动配置，会在java或资源目录下的templates找到hello.html文件
         * 所以可以提前创建好templates文件夹和hello.html文件（默认配置）
         */
        return "hello";
    }
}
