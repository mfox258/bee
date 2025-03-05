package com.mindskip.xzs.controller.scheduling;

import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.service.SchedulingInfoService;
import com.mindskip.xzs.utils.ExcelUtils;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingEditRequest;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.EvaluationWorkbook;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
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
    public void export(@RequestParam("month") String month, HttpServletResponse response) {
        Workbook workbook=this.schedulingInfoService.export(month);
        ExcelUtils.downLoadExcel("scheduling.xls",response,workbook);

    }
}
