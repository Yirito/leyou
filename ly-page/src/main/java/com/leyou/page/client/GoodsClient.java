package com.leyou.page.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;


/**
 * feign远程调用
 * 因为是提供方写接口就好，所以我们调用方继承就好了
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
