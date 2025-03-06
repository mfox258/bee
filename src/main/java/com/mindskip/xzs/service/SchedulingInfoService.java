package com.mindskip.xzs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mindskip.xzs.domain.SchedulingInfo;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingEditRequest;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingStatisticsResponse;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;


public interface SchedulingInfoService extends IService<SchedulingInfo> {
    void edit(SchedulingEditRequest request);

    Workbook export(String month);

    Workbook exportStatistics(String month);

    List<SchedulingStatisticsResponse> statisticsList(String month);
}
