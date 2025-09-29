
package com.mindskip.xzs.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("t_classes_attendance_mapping")
public class ClassesAttendanceMapping implements Serializable {

    /**
     * 主键ID（自增）
     */
    private Integer id;

    /**
     * 班次（对应表中 classes 字段）
     */
    private String classes;

    /**
     * 考勤（对应表中 attendance_am 字段）
     */
    private String attendanceAm;
    /**
     * 考勤（对应表中 attendance_pm 字段）
     */
    private String attendancePm;

    /**
     * 备注（对应表中 memo 字段）
     */
    private String memo;

}
