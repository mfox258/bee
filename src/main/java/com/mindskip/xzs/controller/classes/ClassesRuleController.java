package com.mindskip.xzs.controller.classes;

import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.domain.ClassesRule;
import com.mindskip.xzs.service.ClassesRuleService;
import com.mindskip.xzs.viewmodel.classes.ClassesRulePageRequestVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班次规则
 */
@RestController("AdminClassesRuleController")
@RequestMapping(value = "/api/admin/classes/rule")
public class ClassesRuleController extends BaseApiController {

    private final ClassesRuleService classesRuleService;

    @Autowired
    public ClassesRuleController(ClassesRuleService classesRuleService) {
        this.classesRuleService = classesRuleService;
    }


    @PostMapping(value = "/page")
    public RestResponse<PageInfo<ClassesRule>> page(@RequestBody ClassesRulePageRequestVM vm) {
        PageInfo<ClassesRule> pageInfo = classesRuleService.page(vm);
        return RestResponse.ok(pageInfo);
    }


    @PostMapping(value = "/edit")
    public RestResponse edit(@RequestBody ClassesRule classesRule) {
        this.classesRuleService.saveOrUpdate(classesRule);
        return RestResponse.ok();
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public RestResponse delete(@PathVariable Integer id) {
        ClassesRule classesRule = classesRuleService.getById(id);
        classesRule.setDeleted(true);
        classesRuleService.updateById(classesRule);
        return RestResponse.ok();
    }

    @RequestMapping(value = "/select/{id}", method = RequestMethod.POST)
    public RestResponse<ClassesRule> select(@PathVariable("id") Integer id) {
        ClassesRule classesRule = classesRuleService.getById(id);
        return RestResponse.ok(classesRule);
    }


}
