package com.mindskip.xzs.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_dict")
public class Dict implements Serializable {

    private static final long serialVersionUID = -7797183521247423117L;
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String value;
    private String type;
    private Boolean deleted;

}
