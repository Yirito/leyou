package com.leyou.order.wxpay;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayStateEnum;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;

    @Autowired
    private PayConfig config;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    public String createPayUrl(Long orderId, Long totalPay, String description) {

        try {
            Map<String, String> data = new HashMap<>();
            //商品描述
            data.put("body", description);
            //订单号
            data.put("out_trade_no", orderId.toString());
            //货币（默认就是人民币）
            //data.put("fee_type", "CNY");
            //总金额
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端ip
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url", config.getNotifyUrl());
            //交易类型为扫码支付
            data.put("trade_type", "NATIVE");

            //利用wxPay工具，完成下单
            Map<String, String> result = wxPay.unifiedOrder(data);

            //判断是否通信失败
            isSuccess(result);

            //检验签名
            isValidSign(result);

            //下单成功，获取支付连接
            String url = result.get("code_url");

            return url;
        } catch (Exception e) {
            log.error("【微信下单】创建预交易订单异常", e);
            return null;
        }
    }

    /**
     * 校验数据
     *
     * @param result
     */
    public void isSuccess(Map<String, String> result) {
        //通信失败
        String returnCode = result.get("return_code");
        if (WXPayConstants.FAIL.equals(returnCode)) {
            log.error("【微信下单】与微信通信失败，失败信息：{}", result.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }

        //下单失败
        String resultCode = result.get("result_code");
        if (WXPayConstants.FAIL.equals(resultCode)) {
            log.error("【微信下单】微信下单业务，错误码：{}，错误信息：{}",
                    result.get("err_code"), result.get("err_code_des"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
    }

    /**
     * 校验签名
     *
     * @param result
     */
    public void isValidSign(Map<String, String> result) {
        try {
            // 重新生成签名
            boolean boo1 = WXPayUtil.isSignatureValid(result, config.getKey(), WXPayConstants.SignType.HMACSHA256);
            boolean boo2 = WXPayUtil.isSignatureValid(result, config.getKey(), WXPayConstants.SignType.MD5);

            // 和传过来的签名进行比较
            // 因为不知道返回来的签名是md5还是sha256，所以只要两个签名中没有一个是对的就认为是假的。
            if (!boo1 && !boo2) {
                throw new LyException(ExceptionEnum.WX_SIGN_INVALID);
            }
        } catch (Exception e) {
            log.error("【微信支付】检验签名失败，数据：{}", result);
            throw new LyException(ExceptionEnum.WX_SIGN_INVALID);
        }
    }


    /**
     * 查询订单状态
     *
     * @param orderId
     * @return
     */
    public PayStateEnum queryPayState(Long orderId) {
        try {
            //组织请求参数
            Map<String, String> data = new HashMap<>();
            //订单号
            data.put("out_trade_no", orderId.toString());
            //查询状态
            Map<String, String> result = wxPay.orderQuery(data);
            //校验通信状态
            isSuccess(result);
            //校验签名
            isValidSign(result);
            //校验金额
            String totalFeeStr = result.get("total_fee");  //订单金额
            String outTradeNo = result.get("out_trade_no");  //订单编号
            if (StringUtils.isBlank(totalFeeStr) || StringUtils.isBlank(outTradeNo)) {
                log.error("【微信支付回调】支付回调返回数据不正确");
                throw new LyException(ExceptionEnum.WX_PARAM_INVALID);
            }

            // 获取结果中的金额
            Long totalFee = Long.valueOf(totalFeeStr);

            Order order = orderMapper.selectByPrimaryKey(orderId);

            //  获取订单金额
            //todo 这里验证回调数据时，支付金额使用1分进行验证，后续使用实际支付金额验证
            if (totalFee != 1/*order.getActualPay()*/) {
                log.error("【微信支付回调】支付回调返回数据不正确");
                throw new LyException(ExceptionEnum.WX_PARAM_INVALID);
            }

//        //判断支付状态
//        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(Long.valueOf(outTradeNo));
//
//        if (orderStatus.getStatus() != OrderStatusEnum.PAYED.value()) {
//            //支付成功
//            return;
//        }


            //查询支付状态
            String state = result.get("trade_state");
            if (StringUtils.equals("SUCCESS", state)) {
                //支付成功, 修改支付状态等信息
                // 修改订单状态
                OrderStatus status = new OrderStatus();
                status.setOrderId(orderId);
                status.setStatus(OrderStatusEnum.PAYED.value());
                status.setPaymentTime(new Date());
                int count = orderStatusMapper.updateByPrimaryKeySelective(status);
                if (count != 1) {
                    throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
                }

                log.info("[订单查询]，订单支付成功，订单编号:{}", orderId);

                return PayStateEnum.SUCCESS;
            } else if (StringUtils.equals("USERPAYING", state) || StringUtils.equals("NOTPAY", state)) {
                //未支付成功
                return PayStateEnum.NOT_PAY;
            } else {
                //其他返回付款失败
                return PayStateEnum.FAIL;
            }

        } catch (Exception e) {
            return PayStateEnum.NOT_PAY;
        }
    }
}
