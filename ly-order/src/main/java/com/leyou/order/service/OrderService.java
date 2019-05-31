package com.leyou.order.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDto;
import com.leyou.order.dto.CartDto;
import com.leyou.order.dto.OrderDto;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDeatilMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDeatilMapper deatilMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    public Long createOrder(OrderDto orderDto) {

        // 1 新增订单
        Order order = new Order();
        // 1.1 订单编号,基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDto.getPaymentType());

        // 1.2 用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);//交易成功后才评价留言

        // 1.3 收货人地址
        //获取收货人信息
        AddressDto address = AddressClient.findById(orderDto.getAddressId());
        order.setReceiver(address.getName());
        order.setReceiverAddress(address.getAddress());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverMobile(address.getPhone());
        order.setReceiverState(address.getState());
        order.setReceiverZip(address.getZipCode());

        // 1.4 金额
        List<CartDto> cartDtos = orderDto.getCarts();

        List<Long> ids = cartDtos.stream().map(CartDto::getSkuId).collect(Collectors.toList());
        List<Sku> skuList = goodsClient.querySkuBySpuIds(ids);
        // 2 新增订单详情

        // 3 新增订单状态

        // 4 减库存
        return null;
    }
}
