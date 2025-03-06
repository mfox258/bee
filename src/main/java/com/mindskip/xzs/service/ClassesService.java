package com.mindskip.xzs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.domain.Classes;
import com.mindskip.xzs.viewmodel.classes.ClassesPageRequestVM;

import java.util.List;


public interface ClassesService extends IService<Classes> {

    List<String> selectList(Integer isCount);

    PageInfo<Classes> page(ClassesPageRequestVM vm);
}
