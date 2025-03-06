package com.mindskip.xzs.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mindskip.xzs.domain.ClassesRule;
import com.mindskip.xzs.viewmodel.classes.ClassesRulePageRequestVM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ClassesRuleMapper extends BaseMapper<ClassesRule> {

    List<ClassesRule> classesRulePage(@Param("vm") ClassesRulePageRequestVM vm);
}
