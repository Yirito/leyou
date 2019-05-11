package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsSpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class GoodsSpuController {

    @Autowired
    private GoodsSpuService goodsSpuService;

    /**
     * 分页查询SPU
     *
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,//获取page，默认值为1，转为integer的java代码
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,//required可不传
            @RequestParam(value = "key", required = false) String key) {
        return ResponseEntity.ok(goodsSpuService.querySpuByPage(page, rows, saleable, key));
    }

    /**
     * 商品新增
     *
     * @param spu
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu) {
        goodsSpuService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spu的id查询detail详情
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> queryDetailById(@PathVariable("id") Long spuId){
        return ResponseEntity.ok(goodsSpuService.queryDetailById(spuId));
    }
}
