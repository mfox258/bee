package com.mindskip.xzs.controller.scheduling;

import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.service.SchedulingInfoService;
import com.mindskip.xzs.utils.ExcelUtils;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingEditRequest;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingStatisticsResponse;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController("AdminSchedulingInfoController")
@RequestMapping(value = "/api/admin/scheduling")
public class SchedulingInfoController extends BaseApiController {

    private final SchedulingInfoService schedulingInfoService;

    @Autowired
    public SchedulingInfoController(SchedulingInfoService schedulingInfoService) {
        this.schedulingInfoService = schedulingInfoService;
    }

    @PostMapping(value = "/edit")
    public RestResponse edit(@RequestBody SchedulingEditRequest request) {
        this.schedulingInfoService.edit(request);
        return RestResponse.ok();
    }

    @GetMapping("/export")
    public void export(@RequestParam("startMonth") String startMonth,@RequestParam("endMonth") String endMonth, HttpServletResponse response) {
        Workbook workbook=this.schedulingInfoService.export(startMonth,endMonth);
        ExcelUtils.downLoadExcel("scheduling.xls",response,workbook);

    }

    @GetMapping("/statistics/list")
    public RestResponse<List<SchedulingStatisticsResponse>> statisticsList(@RequestParam("startMonth") String startMonth,@RequestParam("endMonth") String endMonth) {
        List<SchedulingStatisticsResponse> statisticsResponses =  this.schedulingInfoService.statisticsList(startMonth,endMonth);
        return RestResponse.ok(statisticsResponses);
    }
    @GetMapping("/statistics/export")
    public void exportStatistics(@RequestParam("startMonth") String startMonth,@RequestParam("endMonth") String endMonth, HttpServletResponse response) {
        Workbook workbook=this.schedulingInfoService.exportStatistics(startMonth,endMonth);
        ExcelUtils.downLoadExcel("scheduling.xls",response,workbook);

    }

    @GetMapping("/statistic")
    public RestResponse<List<SchedulingStatisticsResponse>> statistic(@RequestParam("startMonth") String startMonth,@RequestParam("endMonth") String endMonth) {
        List<SchedulingStatisticsResponse> statisticsResponses =  this.schedulingInfoService.statistic(startMonth,endMonth);
        return RestResponse.ok(statisticsResponses);
    }

}
