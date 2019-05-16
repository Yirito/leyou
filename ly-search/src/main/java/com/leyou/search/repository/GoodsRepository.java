package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 继承第一个传字节码，第二个是id类型。
 * 类似通用mapper，但比mapper更高级，可以自定义查询
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
