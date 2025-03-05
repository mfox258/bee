package com.mindskip.xzs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.domain.Dict;
import com.mindskip.xzs.viewmodel.dict.DictPageRequestVM;

import java.util.List;


public interface DictService  extends IService<Dict> {

    List<String> list(String type);

    PageInfo<Dict> page(DictPageRequestVM vm);
}
