package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsSpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤
        Example example = new Example(Spu.class);
        //搜索字段过滤
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");
        //查询
        List<Spu> list = spuMapper.selectByExample(example);
        //判断
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }

        /**
         * 解析分类和品牌的名称
         * 就是返回实体结果返回不一样的实体类
         */
        loadCategoryAndBrandName(list);
        //解析分页结果
        PageInfo<Spu> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), list);
    }

    private void loadCategoryAndBrandName(List<Spu> list) {
        for (Spu spu : list) {
            /**
             * 处理分类名称
             * 先更具cid1、2、3查询出list，然后把它丢进流去，获取name，在把他返回name的字符串list
             * StringUtils.join是把LIST按照/进行字符串拼接
             */
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCName(StringUtils.join(names, "/"));
            /**
             * 处理品牌名称
             * 先根据id查询出品牌，在根据品牌名称放入到实体类的bname
             */
            spu.setBName(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    @Transactional
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增detail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        int i = spuDetailMapper.insert(spuDetail);
        if (i != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        //新增sku和库存
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.insert", spu.getId());
    }

    private void saveSkuAndStock(Spu spu) {
        List<Stock> stockList = new ArrayList<>();
        //新增sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            int x = skuMapper.insert(sku);
            if (x != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());//因为需要取到id，所以上面不能弄批量新增
            stock.setStock(sku.getStock());
            stockList.add(stock);
//            int y = stockMapper.insert(stock);
//            if (y != 1) {
//                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
//            }
        }

        //批量新增库存
        int o = stockMapper.insertList(stockList);
        if (o != stockList.size()) {//批量新增
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        //发送mq消息
        //amqpTemplate.convertAndSend("item.insert",spu.getId());
    }

    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOND);
        }
        return spuDetail;
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        //查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }

        //查询库存
//        for (Sku s : skuList) {
//            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
//            if (stock == null) {
//                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOND);
//            }
//            s.setStock(stock.getStock());
//        }


        //查询库存
        //使用map，查询出所有的id集合
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        loadStockInSku(skuList, ids);
        return skuList;
    }

    private void loadStockInSku(List<Sku> skuList, List<Long> ids) {
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOND);
        }
        //我们把stock变成一个map，其key是:sku的id，值是库存值
        Map<Long, Integer> stockMap = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skuList.forEach(s -> s.setStock(stockMap.get(s.getId())));
    }

    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null) {
            throw new LyException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        //查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList)) {
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        //修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        //修改detail
        int i = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (i != 1) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //新增sku和stock
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.update", spu.getId());
    }

    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        //查询sku
        List<Sku> skuList = querySkuBySpuId(id);
        spu.setSkus(skuList);
        //查询detail
        spu.setSpuDetail(queryDetailById(id));
        return spu;
    }

    public List<Sku> querySkuBySpuIds(List<Long> ids) {
        List<Sku> skuList = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOND);
        }
        loadStockInSku(skuList, ids);
        return skuList;
    }
}
