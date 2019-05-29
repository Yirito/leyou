package com.leyou.sms.mq;

import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Slf4j
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {


    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties prop;

    /**
     * 发送短信验证码
     * <p>
     * 这里一定要try（已经在方法try了），不要重试，因为短信有限流，如果你一直重试就限流，就完蛋了
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.queue", durable = "true"),//名字随便起，持久化为true
            exchange = @Exchange(value = "ly.sms.exchange", type = ExchangeTypes.TOPIC),//交换机名字要和提供方保持一致，模型是topic
            key = {"sms.verify.code"}//RoutingKey
    ))
    public void sendSms(Map<String, String> msg) {
        if (CollectionUtils.isEmpty(msg)) {
            return;
        }
        String phone = msg.remove("phone");//remove删除对象并获取元素,这里只剩下code了，然后使用自带的序列化转成json
        if (StringUtils.isBlank(phone)) {
            return;
        }
        smsUtils.sendSms(phone, prop.getSignName(), prop.getVerifyCodeTemplate(), JsonUtils.toString(msg));

        //记录发送短信日志
        log.info("[短信服务],发送短信验证码,手机号:{}", phone);
    }


}
