
package com.mindskip.xzs.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_classes_statistic_rule")
public class ClassesStatisticRule implements Serializable {

    private String classes;
    private String statisticClasses;
    private Double ratio;

}
