package com.leyou.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ly.sms")//获取自定义属性
@Data
public class SmsProperties {

    String accessKeyId;
    String accessKeySecret;
    String signName;
    String verifyCodeTemplate;

}
