package com.leyou.order.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDto;
import com.leyou.common.dto.CartDto;
import com.leyou.order.dto.OrderDto;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.wxpay.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;

    @Transactional
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
        //把cartDTO转为一个map，key是sku的id，值是num
        Map<Long, Integer> numMap = orderDto.getCarts().stream()
                .collect(Collectors.toMap(CartDto::getSkuId, CartDto::getNum));
        //获取所有sku的id
        Set<Long> ids = numMap.keySet();
        //把set的ids转为list ：new ArrayList<>(ids))，然后根据ids查询数据库
        List<Sku> skus = goodsClient.querySkuBySpuIds(new ArrayList<>(ids));

        //准备orderDetail集合
        List<OrderDetail> details = new ArrayList<>();

        long totalPay = 0L;
        for (Sku sku : skus) {
            //计算商品总价
            totalPay += sku.getPrice() * numMap.get(sku.getId());

            //封装orderDetail
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            detail.setNum(numMap.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());
            details.add(detail);
        }

        order.setTotalPay(totalPay);
        //实付金额：总金额+邮费-优惠金额（这里0）
        order.setActualPay(totalPay + order.getPostFee() - 0);

        // 1.5 order写入数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1) {
            log.error("[创建订单功能] 创建订单失败,orderId:{}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 2 新增订单详情
        count = detailMapper.insertList(details);
        if (count != details.size()) {
            log.error("[创建订单功能] 创建订单详情失败,orderId:{}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 3 新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setOrderId(orderId);
        //因为数字太多，很多人不认识1、2、3对应的是什么，所以弄枚举。枚举加了desc描述，可以不加，加了让别人知道是什么来的
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        count = orderStatusMapper.insertSelective(orderStatus);
        if (count != 1) {
            log.error("[创建订单功能] 创建订单状态失败,orderId:{}", orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        // 4 减库存
        List<CartDto> cartDTOS = orderDto.getCarts();
        //这里采用同步减库存：远程调用。而不是异步，事务MQ。因为同步如果失败事务会回滚，但异步失败不会回滚。
        goodsClient.decreaseStock(cartDTOS);

        return orderId;
    }

    public Order queryOrderById(Long id) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            // 不存在
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = detailMapper.select(detail);
        if (CollectionUtils.isEmpty(details)) {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(details);
        //查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        if (orderStatus == null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    public String createPayUrl(Long orderId) {
        //查询订单
        Order order = queryOrderById(orderId);
        //判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UN_PAY.value()) {
            //订单状态异常
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //支付金额
        Long actualPay = order.getActualPay();
        //商品描述
        OrderDetail detail = order.getOrderDetails().get(0);
        String desc = detail.getTitle();

        return payHelper.createPayUrl(orderId, actualPay, desc);
    }

    /**
     * 处理回调
     *
     * @param result 接收数据
     */
    public void handleNotify(Map<String, String> result) {
        // 1 校验数据
        payHelper.isSuccess(result);
        // 2 校验签名
        payHelper.isValidSign(result);
        // 3 校验金额
        // 3.1 解析数据
        String totalFeeStr = result.get("total_fee");  //订单金额
        String outTradeNo = result.get("out_trade_no");  //订单编号
        String transactionId = result.get("transaction_id");  //商户订单号
        String bankType = result.get("bank_type");  //银行类型
        if (StringUtils.isBlank(totalFeeStr) || StringUtils.isBlank(outTradeNo)
                || StringUtils.isBlank(transactionId) || StringUtils.isBlank(bankType)) {
            log.error("【微信支付回调】支付回调返回数据不正确");
            throw new LyException(ExceptionEnum.WX_PARAM_INVALID);
        }

        // 3.2 获取结果中的金额
        Long totalFee = Long.valueOf(totalFeeStr);
        // 3.3 获取订单id
        Long orderId = Long.valueOf(outTradeNo);

        Order order = orderMapper.selectByPrimaryKey(orderId);

        // 3.4 金额校验
        //todo 这里验证回调数据时，支付金额使用1分进行验证，后续使用实际支付金额验证
        if (totalFee != 1/*order.getActualPay()*/) {
            log.error("【微信支付回调】支付回调返回数据不正确");
            throw new LyException(ExceptionEnum.WX_PARAM_INVALID);
        }

        // 4 修改订单状态
        OrderStatus status = new OrderStatus();
        status.setOrderId(orderId);
        status.setStatus(OrderStatusEnum.PAYED.value());
        status.setPaymentTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(status);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }

        log.info("[订单回调]，订单支付成功，订单编号:{}", orderId);
    }
}
