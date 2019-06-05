package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_spec_param")
public class SpecParam {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    private Long cid;
    private Long groupId;
    private String name;
    @Column(name = "`numeric`")//告诉通用mapper，sql语句的时候，使用`numeric`查询，而不是numeric，避免产生歧义（转义）
    private Boolean numeric;
    private String unit;
    private Boolean generic;
    private Boolean searching;
    private String segments;
}
