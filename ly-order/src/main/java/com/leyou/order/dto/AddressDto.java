package com.leyou.order.dto;

import lombok.Data;

/**
 * 这个是假的收货地址，因为真实的需要从数据库里面取出来，暂未做收货人地址的数据
 */
@Data
public class AddressDto {

    private Long id;
    private String name;//收件人姓名
    private String phone;//电话
    private String state;//省份
    private String city;//城市
    private String district;//区
    private String address;//街道地址
    private String zipCode;//邮编
    private Boolean isDefault;
}
