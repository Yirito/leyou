package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import com.leyou.order.wxpay.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;

    /**
     * 微信支付成功回调
     *
     * @param result
     * @return
     */
    @PostMapping(value = "pay", produces = "application/xml")//produces 声明返回必须是xml格式
    @ResponseBody//把响应结果序列化之后放到body，不一定转json。消息转换器默认是转json
    //下面的@RequestBody 是相反。把body反序列化。
    // 直接转xml有错，因为没有xml转换器，需要引入xml依赖。引入之后spring会自动判断。
    public Map<String, String> hello(@RequestBody Map<String, String> result) {
        //处理回调
        orderService.handleNotify(result);

        log.info("[支付回调] 接收微信支付回调,结果:{}", result);

//        String message = "<xml>\n" +
//                "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
//                "  <return_msg><![CDATA[OK]]></return_msg>\n" +
//                "</xml>";

        //两种方式返回，可以上面,返回String。因为引入了xml转换器，所以可以使用Map返回，spring会自动转xml
        HashMap<String, String> msg = new HashMap<>();
        msg.put("return_code", "SUCCESS");
        msg.put("return_msg", "OK");
        return msg;
    }
}
