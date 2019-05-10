package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    /**
     * 因为通用mapper只能使用单表，所以中间表需要我们自己写。
     * 使用注解插入数据库语句
     *
     * @param cid
     * @param id
     * @return
     */
    @Insert("INSERT INTO tb_category_brand (category_id,brand_id) VALUES(#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid") Long cid, @Param("bid") Long id);

    /**
     * 这里有个中间表
     * sql自己写,两边关联
     * <p>
     * SELECT b.* FROM tb_category_brand cb INNER JOIN tb_brand b ON b.id = cb.brand_id WHERE cb.category_id = = ?
     *
     * @param cid
     * @return
     */
    @Select("SELECT b.* FROM tb_category_brand cb INNER JOIN tb_brand b ON b.id = cb.brand_id WHERE cb.category_id = #{cid}")
    List<Brand> queryByCategoryId(@Param("cid") Long cid);
}
