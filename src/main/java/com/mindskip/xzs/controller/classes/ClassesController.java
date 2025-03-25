package com.mindskip.xzs.controller.classes;

import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.aspect.annotation.LogRecord;
import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.domain.Classes;
import com.mindskip.xzs.service.ClassesService;
import com.mindskip.xzs.viewmodel.classes.ClassesPageRequestVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班次
 */
@RestController("AdminClassesController")
@RequestMapping(value = "/api/admin/classes")
public class ClassesController extends BaseApiController {

    private final ClassesService classesService;

    @Autowired
    public ClassesController(ClassesService classesService) {
        this.classesService = classesService;
    }


    @PostMapping(value = "/page")
    public RestResponse<PageInfo<Classes>> page(@RequestBody ClassesPageRequestVM vm) {
        PageInfo<Classes> pageInfo = classesService.page(vm);
        return RestResponse.ok(pageInfo);
    }
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public RestResponse<List<String>> list(@RequestParam(value = "isCount",required = false) Integer isCount) {
        List<String> pageInfo = classesService.selectList(isCount);
        return RestResponse.ok(pageInfo);
    }


    @PostMapping(value = "/edit")
    public RestResponse edit(@RequestBody Classes classes) {
        this.classesService.saveOrUpdate(classes);
        return RestResponse.ok();
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public RestResponse delete(@PathVariable Integer id) {
        Classes classes = classesService.getById(id);
        classes.setDeleted(true);
        classesService.updateById(classes);
        return RestResponse.ok();
    }

    @RequestMapping(value = "/select/{id}", method = RequestMethod.POST)
    public RestResponse<Classes> select(@PathVariable("id") Integer id) {
        Classes classes = classesService.getById(id);
        return RestResponse.ok(classes);
    }
}
