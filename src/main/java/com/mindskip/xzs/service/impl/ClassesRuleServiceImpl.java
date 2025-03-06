package com.mindskip.xzs.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.domain.ClassesRule;
import com.mindskip.xzs.repository.ClassesRuleMapper;
import com.mindskip.xzs.service.ClassesRuleService;
import com.mindskip.xzs.viewmodel.classes.ClassesRulePageRequestVM;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassesRuleServiceImpl extends ServiceImpl<ClassesRuleMapper, ClassesRule> implements ClassesRuleService {

    @Override
    public PageInfo<ClassesRule> page(ClassesRulePageRequestVM vm) {
        return PageHelper.startPage(vm.getPageIndex(), vm.getPageSize(), "id asc").doSelectPageInfo(() ->
                this.baseMapper.classesRulePage(vm)
        );
    }

    @Override
    public List<ClassesRule> selectList() {
        return this.list(Wrappers.<ClassesRule>lambdaQuery().eq(ClassesRule::getDeleted,0));
    }
}
