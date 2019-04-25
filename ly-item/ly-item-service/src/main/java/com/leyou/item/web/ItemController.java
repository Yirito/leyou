package com.leyou.item.web;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.pojo.Item;
import com.leyou.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用来测试item的controller
 */
@RestController
@RequestMapping("item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping
    public ResponseEntity<Item> saveItem(Item item) {
        //校验价格
        if (item.getPrice() == null) {
            //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

            //使用自定义common包下的拦截LyException,不然状态码和友好提示都不好识别
            throw new LyException(ExceptionEnum.PRICE_CANNOT_BE_NULL);
        }
        Item reItem = itemService.saveItem(item);
        /**
         * 标准的REST返回结果。
         * 标准的请求不能加动词，只能名词，如：http：//127.0.0.1/item，而不能http：//127.0.0.1/itemSave
         */
        return ResponseEntity.status(HttpStatus.CREATED).body(reItem);
    }
}
