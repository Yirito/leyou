package com.leyou.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "ly.filter")
public class FilterProperties {

    /**
     * 属性集合
     */
    private List<String> allowPaths;
}
