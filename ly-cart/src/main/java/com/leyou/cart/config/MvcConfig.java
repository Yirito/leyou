package com.leyou.cart.config;

import com.leyou.cart.interceptor.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcConfigurer
 * 要想拦截器生效，需要继承WebMvcConfigurer，然后@Configuration
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtProperties prop;

    /**
     * 添加一个拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * 这里不能直接new，因为UserInterceptor中使用spring注入获取配置文件，所以需要他自己去new，而这里我们new的话会出错
         * 所以把UserInterceptor注入的属性抽取出来，放到这个类里面。如获取配置文件以及注入prop
         */
        registry.addInterceptor(new UserInterceptor(prop)).addPathPatterns("/**");
    }
}
