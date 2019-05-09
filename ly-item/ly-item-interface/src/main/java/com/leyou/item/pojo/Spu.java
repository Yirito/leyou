package com.leyou.item.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * spu表
 */
@Data
@Table(name = "tb_spu")
public class Spu {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    private Long brandId;
    private Long cid1;//1级类目
    private Long cid2;//2级类目
    private Long cid3;//3级类目
    private String title;//标题
    private String subTitle;//子标题
    private Boolean saleable;//是否上架

    @JsonIgnore//返回页面的时候，忽略这个字段
    private Boolean valid;//是否有效，逻辑删除用
    private Date createTime;//创建时间

    @JsonIgnore//返回页面的时候，忽略这个字段
    private Date lastUpdateTime;//最后修改时间

    /**
     * 是persistence.Transient;
     * 不是javaBean的Transient包
     * 告诉通用mapper，这个不用转为数据库字段，因为数据库没有这两个字段
     * 正常开发不能写成这样，要写vo层。
     * 不能告诉别人你数据库是什么，vo层和这个实体类很像，但名字比如cid1，要改为不认识的，比如spId。这样就能防止别人知道你数据库结构了
     */
    @Transient
    private String cName;
    @Transient
    private String bName;
}
