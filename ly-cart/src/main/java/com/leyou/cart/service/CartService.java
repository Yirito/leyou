package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<Cart> queryCartList() {
        //获取登陆用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();

        //判断是否存在
        if (!redisTemplate.hasKey(key)) {
            //key不存在,返回404
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }

        //获取登陆用户的所有购物车
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);//boundHashOps方便简写
        List<Cart> cartList = operation.values().stream()
                .map(obj -> JsonUtils.toBean(obj.toString(), Cart.class)).collect(Collectors.toList());

        return cartList;
    }

    public void updateCartNum(Long skuId, Integer num) {
        //获取登陆用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();
        //hashKey
        String hashKey = skuId.toString();
        //获取操作
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);

        //判断是否存在
        if (!redisTemplate.hasKey(key)) {
            //key不存在,返回404
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }
        //查询购物车
        Cart cart = JsonUtils.toBean(operations.get(hashKey).toString(), Cart.class);
        cart.setNum(num);
        //写回redis
        operations.put(hashKey, JsonUtils.toString(cart));
    }

    public void deleteCart(Long skuId) {
        //获取登陆用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();

        //删除
        redisTemplate.opsForHash().delete(key, skuId.toString());
    }
}
