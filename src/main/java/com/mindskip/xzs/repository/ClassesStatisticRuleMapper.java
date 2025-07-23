package com.mindskip.xzs.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mindskip.xzs.domain.ClassesStatisticRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface ClassesStatisticRuleMapper extends BaseMapper<ClassesStatisticRule> {

    @Select("select distinct statistic_classes  from t_classes_statistic_rule where ratio is not null and statistic_classes!='休假' ")
    List<String> list();
}
