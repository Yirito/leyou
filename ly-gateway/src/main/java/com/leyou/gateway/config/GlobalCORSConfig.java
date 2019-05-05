package com.leyou.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCORSConfig {

    /**
     * CORS跨域请求
     *
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        //1、添加CORS配置信息
        CorsConfiguration configuration = new CorsConfiguration();
        //1)允许的域，不要写*，否则cookie就无法使用了
        configuration.addAllowedOrigin("http://manage.leyou.com");
        //2)是否发送Cookie信息
        configuration.setAllowCredentials(true);
        //3)允许的请求方式
        configuration.addAllowedMethod(HttpMethod.OPTIONS);
        configuration.addAllowedMethod(HttpMethod.HEAD);
        configuration.addAllowedMethod(HttpMethod.GET);
        configuration.addAllowedMethod(HttpMethod.PUT);
        configuration.addAllowedMethod(HttpMethod.POST);
        configuration.addAllowedMethod(HttpMethod.DELETE);
        configuration.addAllowedMethod(HttpMethod.PATCH);
        //4)允许的头信息
        configuration.addAllowedMethod("*");
        //5)有效时长
        configuration.setMaxAge(3600L);

        //2、添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", configuration);

        //3、返回新的CORSFilter
        return new CorsFilter(configurationSource);
    }
}
