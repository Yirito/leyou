package com.leyou.item.Vo;

import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import java.util.Date;

/**
 * VO层，Spu的VO
 * 返回数据不能为数据库，所以这里要转成非数据库的实体类，防止别人知道你数据库是什么
 * 所以正常开发的时候，转实体类会很麻烦，要转来转去
 */
public class SpuVo {

    private Long id;
    private Long brandId;
    private Long cid1;//1级类目
    private Long cid2;//2级类目
    private Long cid3;//3级类目
    private String title;//标题
    private String subTitle;//子标题
    private Boolean saleable;//是否上架
    private Boolean valid;//是否有效，逻辑删除用
    private Date createTime;//创建时间
    private Date lastUpdateTime;//最后修改时间
    private String cName;
    private String sName;
}
