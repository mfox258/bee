package com.mindskip.xzs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.domain.ClassesRule;
import com.mindskip.xzs.viewmodel.classes.ClassesRulePageRequestVM;

import java.util.List;


public interface ClassesRuleService extends IService<ClassesRule> {

    /**
     * 分页查询班次规则
     * @param vm
     * @return
     */
    PageInfo<ClassesRule> page(ClassesRulePageRequestVM vm);

    List<ClassesRule> selectList();
}
