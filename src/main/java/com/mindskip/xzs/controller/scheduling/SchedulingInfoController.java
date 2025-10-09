package com.mindskip.xzs.controller.scheduling;

import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.service.SchedulingInfoService;
import com.mindskip.xzs.utils.ExcelUtils;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingEditRequest;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingStatisticsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


@Slf4j
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

    /**
     * 导出 scheduling 信息
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @param response HttpServletResponse
     */
    @GetMapping("/export")
    public void export(@RequestParam("startMonth") String startMonth,@RequestParam("endMonth") String endMonth, HttpServletResponse response) {
        Workbook workbook=this.schedulingInfoService.export(startMonth,endMonth);
        ExcelUtils.downLoadExcel("scheduling.xls",response,workbook);

    }

    /**
     * 获取 scheduling 统计信息列表
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @return 统计信息列表
     */
    @GetMapping("/statistics/list")
    public RestResponse<List<SchedulingStatisticsResponse>> statisticsList(@RequestParam("startMonth") String startMonth,@RequestParam("endMonth") String endMonth) {
        List<SchedulingStatisticsResponse> statisticsResponses =  this.schedulingInfoService.statisticsList(startMonth,endMonth);
        return RestResponse.ok(statisticsResponses);
    }
    /**
     * 导出 scheduling 统计信息
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @param response HttpServletResponse
     */
    @GetMapping("/statistics/export")
    public void exportStatistics(@RequestParam("startMonth") String startMonth,@RequestParam("endMonth") String endMonth, HttpServletResponse response) {
        Workbook workbook=this.schedulingInfoService.exportStatistics(startMonth,endMonth);
        ExcelUtils.downLoadExcel("scheduling.xls",response,workbook);

    }

    /**
     * 获取 scheduling 统计信息
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @return 统计信息列表
     */
    @GetMapping("/statistic")
    public RestResponse<List<SchedulingStatisticsResponse>> statistic(@RequestParam("startMonth") String startMonth,@RequestParam("endMonth") String endMonth) {
        List<SchedulingStatisticsResponse> statisticsResponses =  this.schedulingInfoService.statistic(startMonth,endMonth);
        return RestResponse.ok(statisticsResponses);
    }

    /**
     * 导出 scheduling 出勤信息
     * @param year 日期
     * @param month 日期
     * @param response HttpServletResponse
     */
    @GetMapping("/download/attendance")
    public void downloadAttendance(@RequestParam("year") Integer year,@RequestParam("month") Integer month,HttpServletResponse response) {
        try {
            Workbook workbook = this.schedulingInfoService.exportAttendance(year, month);
            if (workbook == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：模板文件不存在或数据异常\"}");
                return;
            }
            ExcelUtils.downLoadExcel("scheduling_attendance.xls", response, workbook);
        } catch (Exception e) {
            log.error("导出出勤信息异常", e);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    /**
     * 导出 scheduling 加班信息
     * @param year 日期
     * @param month 日期
     * @param response HttpServletResponse
     */
    @GetMapping("/download/overtime")
    public void downloadOvertime(@RequestParam("year") Integer year,@RequestParam("month") Integer month,HttpServletResponse response) {
        try {
            Workbook workbook = this.schedulingInfoService.exportOvertime(year, month);
            if (workbook == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：模板文件不存在或数据异常\"}");
                return;
            }
            ExcelUtils.downLoadExcel("scheduling_overtime.xlsx", response, workbook);
        } catch (Exception e) {
            log.error("导出加班信息异常", e);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

}
