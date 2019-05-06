package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter//为非final字段添加
@NoArgsConstructor //自动生成无参数构造函数。
@AllArgsConstructor //自动生成全参数构造函数。
public enum ExceptionEnum {

    /**
     * 这个为构造函数，简化的构造函数 private static final ExceptionEnum ff = new ExceptionEnum(1,"123");
     * 多个枚举按照,隔开，最后一个必须为;
     */
    PRICE_CANNOT_BE_NULL(400, "价格不能为空"),
    CATEGORY_NOT_FOND(404,"商品分类没有查到"),
    BRAND_NOT_FOUND(404,"品牌不存在"),
    ;
    private int code;
    private String msg;
}
