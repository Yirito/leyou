package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * 根据主键字符串进行查询，类中只有存在一个带有@Id注解的字段
 * IdListMapper为处理根据id获取信息，第二个参数为id的类型，这里为Long
 */
public interface CategoryMapper extends Mapper<Category>, IdListMapper<Category, Long> {
}
