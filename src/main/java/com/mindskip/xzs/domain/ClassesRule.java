package com.mindskip.xzs.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("t_classes_rule")
public class ClassesRule implements Serializable {

    private static final long serialVersionUID = -7797183521247423117L;
    @TableId(type = IdType.AUTO)
    private Long id;

    private String classes;
    private String targetClasses;
    private BigDecimal ratio;
    private Boolean deleted;

}
