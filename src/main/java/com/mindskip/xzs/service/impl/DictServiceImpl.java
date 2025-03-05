package com.mindskip.xzs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.domain.Dict;
import com.mindskip.xzs.repository.DictMapper;
import com.mindskip.xzs.service.DictService;
import com.mindskip.xzs.viewmodel.dict.DictPageRequestVM;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DictServiceImpl  extends ServiceImpl<DictMapper, Dict> implements DictService {

    private final DictMapper dictMapper;

    @Override
    public List<String> list(String type) {
        return this.dictMapper.list(type);
    }

    @Override
    public PageInfo<Dict> page(DictPageRequestVM vm) {
        return PageHelper.startPage(vm.getPageIndex(), vm.getPageSize(), "id asc").doSelectPageInfo(() ->
                dictMapper.dictPage(vm)
        );
    }
}
