package com.leyou.search.mq;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {

    @Autowired
    private SearchService searchService;

    /**
     * 这里不要处理异常，也不要写方法里面处理异常，让他自生自灭。这样消息不通过，不会确认消息，发送方会收到并重试
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.insert.queue", durable = "true"),//名字随便起，持久化为true
            exchange = @Exchange(value = "ly.item.exchange", type = ExchangeTypes.TOPIC),//交换机名字要和提供方保持一致，模型是topic
            key = {"item.insert", "item.update"}//RoutingKey
    ))
    public void listenInsertOrUpdate(Long spuId) {
        if (spuId == null) {
            return;
        }
        //处理消息，对索引库进行新增或修改
        searchService.createOrUpdateIndex(spuId);
    }

    /**
     * 这里不要处理异常，也不要写方法里面处理异常，让他自生自灭。这样消息不通过，不会确认消息，发送方会收到并重试
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.delete.queue", durable = "true"),//名字随便起，持久化为true
            exchange = @Exchange(value = "ly.item.exchange", type = ExchangeTypes.TOPIC),//交换机名字要和提供方保持一致，模型是topic
            key = {"item.delete"}//RoutingKey
    ))
    public void listenDelete(Long spuId) {
        if (spuId == null) {
            return;
        }
        //处理消息，对索引库进行删除
        searchService.deleteIndex(spuId);
    }

}
