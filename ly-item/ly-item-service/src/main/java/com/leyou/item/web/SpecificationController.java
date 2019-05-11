package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据分类id查询规格组
     * 注意PathVariable
     * localhost:10010/........../spec/groups/查询的cid
     *
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid) {
        return ResponseEntity.ok(specificationService.queryGroupByCid(cid));
    }

    /**
     * 根据组id查询参数
     * 注意@RequestParam
     * localhost:10010/........../spec/params?查询的gid
     *
     * 最新，因为要根据组id查询参数、根据分类id查询参数，有两个相同的，所以不便写两个controller，合二为一，改为下面的。
     * @param gid
     * @return
     */
    //@GetMapping("params")
    //public ResponseEntity<List<SpecParam>> queryParamByGid(@RequestParam("gid") Long gid) {
    //    return ResponseEntity.ok(specificationService.queryParamGid(gid));
    //}

    /**
     * 查询参数集合
     * 最新，因为要根据组id查询参数、根据分类id查询参数，有两个相同的，所以不便写两个controller，合二为一。
     * searching预留非以后
     *
     * @param gid 组id
     * @param cid 分类id
     * @param searching 是否搜索
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamList(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching) {
        return ResponseEntity.ok(specificationService.queryParamList(gid,cid,searching));
    }
}
