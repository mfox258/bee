package com.mindskip.xzs.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
@TableName("t_scheduling_info")
public class SchedulingInfo implements Serializable {

    private static final long serialVersionUID = -7797183521247423117L;
    @TableId(type = IdType.AUTO)
    private Long id;


    private String userName;

    private String date;

    private String month;

    private String classes;


}
