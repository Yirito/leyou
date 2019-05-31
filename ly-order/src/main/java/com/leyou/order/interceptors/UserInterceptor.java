package com.leyou.order.interceptors;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 要想实现这个，需要写一个类继承WebMvcConfigurer
 */
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties prop;

    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    /**
     * 前置拦截  还有后置拦截等
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //获取cookie中的token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
            //解析token
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            //传递user
            //request.setAttribute("user", user);
            tl.set(user);
            //放行
            return true;
        } catch (Exception e) {
            log.error("[购物车服务],用户未登录。", e);
            return false;
        }
    }

    /**
     * 拦截最终完成
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //最后用完数据，一定要清空。
        tl.remove();
    }

    public static UserInfo getUser() {
        return tl.get();
    }
}
