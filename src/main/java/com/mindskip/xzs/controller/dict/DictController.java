package com.mindskip.xzs.controller.dict;

import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.domain.Dict;
import com.mindskip.xzs.service.DictService;
import com.mindskip.xzs.viewmodel.dict.DictPageRequestVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController("AdminDictController")
@RequestMapping(value = "/api/admin/dict")
public class DictController extends BaseApiController {

    private final DictService dictService;

    @Autowired
    public DictController(DictService dictService) {
        this.dictService = dictService;
    }


    @PostMapping(value = "/page")
    public RestResponse<PageInfo<Dict>> page(@RequestBody DictPageRequestVM vm) {
        PageInfo<Dict> pageInfo = dictService.page(vm);
        return RestResponse.ok(pageInfo);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public RestResponse<List<String>> list(@RequestParam("type") String type) {
        List<String> pageInfo = dictService.list(type);
        return RestResponse.ok(pageInfo);
    }


    @PostMapping(value = "/edit")
    public RestResponse edit(@RequestBody Dict dict) {
        this.dictService.saveOrUpdate(dict);
        return RestResponse.ok();
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public RestResponse delete(@PathVariable Integer id) {
        Dict dict = dictService.getById(id);
        dict.setDeleted(true);
        dictService.updateById(dict);
        return RestResponse.ok();
    }

    @RequestMapping(value = "/select/{id}", method = RequestMethod.POST)
    public RestResponse<Dict> select(@PathVariable("id") Integer id) {
        Dict dict = dictService.getById(id);
        return RestResponse.ok(dict);
    }

}
