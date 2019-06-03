package com.leyou.order.wxpay;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;

    @Autowired
    private PayConfig config;

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


            //下单成功，获取支付连接
            String url = result.get("code_url");

            return url;
        } catch (Exception e) {
            log.error("【微信下单】创建预交易订单异常", e);
            return null;
        }
    }

}
