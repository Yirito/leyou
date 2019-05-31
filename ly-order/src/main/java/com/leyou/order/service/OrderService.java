package com.leyou.order.service;

import com.leyou.common.utils.IdWorker;
import com.leyou.order.dto.OrderDto;
import com.leyou.order.mapper.OrderDeatilMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Long createOrder(OrderDto orderDto) {

        // 1 新增订单

        // 1.1 订单编号,基本信息
        long orderId = idWorker.nextId();
        // 1.2 用户信息

        // 1.3 收货人地址

        // 1.4 金额

        // 2 新增订单详情

        // 3 新增订单状态

        // 4 减库存
        return null;
    }
}
