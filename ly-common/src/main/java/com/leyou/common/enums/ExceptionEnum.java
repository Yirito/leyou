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
    BRAND_NOT_FOUND(404, "品牌不存在"),
    BRAND_SAVE_ERROR(500, "新增品牌失败"),
    UPLOAD_FILE_ERROR(500, "文件上传失败"),
    INVALID_FILE_TYPE(500, "无效的文件类型"),
    SPEC_GROUP_NOT_FOUND(404, "商品规格组不存在"),
    ;
    private int code;
    private String msg;
}
