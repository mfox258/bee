package com.mindskip.xzs.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mindskip.xzs.domain.Dict;
import com.mindskip.xzs.viewmodel.dict.DictPageRequestVM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface DictMapper  extends BaseMapper<Dict> {


    @Select("select value from t_dict where type =#{type}")
    List<String> list(@Param("type") String type);

    List<Dict> dictPage(@Param("vm") DictPageRequestVM vm);
}
