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
     * <p>
     * 注意，状态码需要符合REST风格
     */
    PRICE_CANNOT_BE_NULL(400, "价格不能为空"),
    CATEGORY_NOT_FOND(404, "商品分类没有查到"),
    GOODS_NOT_FOND(404, "商品不存在"),
    GOODS_DETAIL_NOT_FOND(404, "商品详情不存在"),
    GOODS_SKU_NOT_FOND(404, "商品SKU不存在"),
    GOODS_STOCK_NOT_FOND(404, "商品库存不存在"),
    BRAND_NOT_FOUND(404, "品牌不存在"),
    BRAND_SAVE_ERROR(500, "新增品牌失败"),
    UPLOAD_FILE_ERROR(500, "文件上传失败"),
    INVALID_FILE_TYPE(500, "无效的文件类型"),
    SPEC_GROUP_NOT_FOUND(404, "商品规格组不存在"),
    SPEC_PARAM_NOT_FOUND(404, "商品规格参数不存在"),
    GOODS_SAVE_ERROR(500, "新增商品失败"),
    GOODS_UPDATE_ERROR(500, "更新商品失败"),
    GOODS_ID_CANNOT_BE_NULL(400, "商品id不能为空"),

    INVALID_USER_DATA_TYPE(400, "用户数据类型无效"),
    INVALID_VERIFY_CODE(400, "无效验证码"),
    INVALID_USER_PASSWORD(400, "用户名或密码错误"),
    CREATE_TOKEN_ERROR(500, "用户凭证生成失败"),
    UNAUTHORIZED(403, "未授权"),

    CART_NOT_FOUND(404, "购物车为空"),
    ;
    private int code;
    private String msg;
}
