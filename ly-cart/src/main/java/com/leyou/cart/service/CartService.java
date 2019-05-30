package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:user:id:";

    public void addCart(Cart cart) {
        //获取登陆用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();
        //hashKey
        String hashKey = cart.getSukId().toString();
        //记录num
        Integer num = cart.getNum();

        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);//boundHashOps方便简写
        //判断当前购物车商品，是否存在
        if (operation.hasKey(hashKey)) {
            //存在，修改数量
            String json = operation.get(hashKey).toString();
            cart = JsonUtils.toBean(json, Cart.class);
            cart.setNum(cart.getNum() + num);
        }
        //写回redis
        operation.put(hashKey, JsonUtils.toString(cart));
    }

}
