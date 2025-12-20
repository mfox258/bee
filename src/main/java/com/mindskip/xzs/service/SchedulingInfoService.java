package com.mindskip.xzs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mindskip.xzs.domain.SchedulingInfo;
import com.mindskip.xzs.domain.User;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingEditRequest;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingStatisticsResponse;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;


public interface SchedulingInfoService extends IService<SchedulingInfo> {
    void edit(SchedulingEditRequest request);

    Workbook export(String startMonth, String endMonth, User currentUser);

    Workbook exportStatistics(String startMonth, String endMonth, User currentUser);

    List<SchedulingStatisticsResponse> statisticsList(String startMonth, String endMonth, User currentUser);

    List<SchedulingStatisticsResponse> statistic(String startMonth, String endMonth, User currentUser);

    /**
     * 导出 scheduling 出勤信息
     * @param year 日期
     * @param month 日期
     * @return Workbook
     */
    Workbook exportAttendance(Integer year,Integer month);

     /**
      * 导出 scheduling 加班信息
      *
      * @param year        日期
      * @param month       日期
      * @param currentUser
      * @return Workbook
      */
    Workbook exportOvertime(Integer year, Integer month, User currentUser);
}
