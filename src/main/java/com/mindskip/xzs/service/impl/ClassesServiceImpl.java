package com.mindskip.xzs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.domain.Classes;
import com.mindskip.xzs.repository.ClassesMapper;
import com.mindskip.xzs.service.ClassesService;
import com.mindskip.xzs.viewmodel.classes.ClassesPageRequestVM;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ClassesServiceImpl extends ServiceImpl<ClassesMapper, Classes> implements ClassesService {

    private final ClassesMapper classesMapper;

    @Override
    public List<String> selectList(Integer isCount) {
        return this.classesMapper.list(isCount);
    }

    @Override
    public PageInfo<Classes> page(ClassesPageRequestVM vm) {
        return PageHelper.startPage(vm.getPageIndex(), vm.getPageSize(), "id asc").doSelectPageInfo(() ->
                classesMapper.classesPage(vm)
        );
    }
}
