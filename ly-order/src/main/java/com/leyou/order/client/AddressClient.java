package com.leyou.order.client;

import com.leyou.order.dto.AddressDto;

import java.util.ArrayList;
import java.util.List;

/**
 * 假数据
 * 可以自己写个远程调用查询收货人。但没做。
 */
public abstract class AddressClient {

    public static final List<AddressDto> addressList = new ArrayList<AddressDto>() {
        {
            AddressDto address = new AddressDto();
            address.setId(1L);
            address.setAddress("清新大楼666");
            address.setCity("深圳");
            address.setDistrict("龙华区");
            address.setName("禧爷");
            address.setPhone("13800138000");
            address.setState("广东省");
            address.setZipCode("517000");
            address.setIsDefault(true);
            add(address);

            AddressDto address2 = new AddressDto();
            address2.setId(2L);
            address2.setAddress("苦逼大楼999");
            address2.setCity("深圳");
            address2.setDistrict("宝安区");
            address2.setName("凤哥哥");
            address2.setPhone("159753789456");
            address2.setState("广东省");
            address2.setZipCode("517000");
            address2.setIsDefault(true);
            add(address2);
        }
    };

    public static AddressDto findById(Long id) {
        for (AddressDto addressDto : addressList) {
            if (addressDto.getId() == id)
                return addressDto;
        }
        return null;
    }
}
