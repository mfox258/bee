package com.mindskip.xzs.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mindskip.xzs.domain.Classes;
import com.mindskip.xzs.viewmodel.classes.ClassesPageRequestVM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ClassesMapper extends BaseMapper<Classes> {

    List<String> list(@Param("isCount") Integer isCount, @Param("targetClasses") String targetClasses,@Param("color")  Integer color);

    List<Classes> classesPage(@Param("vm") ClassesPageRequestVM vm);
}
