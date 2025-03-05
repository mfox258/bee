package com.mindskip.xzs.utils;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.handler.inter.IExcelExportServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCells;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

/**
 * @Description: EXCEL导出工具
 * @Date: 2021/5/8
 *
 * @author sea
 * @since JDK 1.8
 */
@Slf4j
public class ExcelUtils {
    private final static String EXCEL2003 = "xls";
    private final static String EXCEL2007 = "xlsx";
    public static void exportExcel(List<?> list, String title, String sheetName, Class<?> pojoClass,
                                   String fileName, boolean isCreateHeader, HttpServletResponse response) {
        ExportParams exportParams = new ExportParams(title, sheetName);
        exportParams.setCreateHeadRows(isCreateHeader);
        defaultExport(list, pojoClass, fileName, response, exportParams);
    }

    public static void exportExcel(List<?> list, String sheetName, Class<?> pojoClass,
                                   String fileName, boolean isCreateHeader, HttpServletResponse response) {
        ExportParams exportParams = new ExportParams(null, sheetName);
        exportParams.setCreateHeadRows(isCreateHeader);
        defaultExport(list, pojoClass, fileName, response, exportParams);
    }

    /**
     * 导出大表格
     * @param list
     * @param sheetName
     * @param pojoClass
     * @param fileName
     * @param isCreateHeader
     * @param response
     */
    public static void exportBigExcel(List<?> list, String sheetName, Class<?> pojoClass,
                                   String fileName, boolean isCreateHeader, HttpServletResponse response) {
        ExportParams exportParams = new ExportParams(null, sheetName,ExcelType.XSSF);
        exportParams.setCreateHeadRows(isCreateHeader);
        bigExcelExport(list, pojoClass, fileName, response, exportParams);
    }

    public static void exportExcel(List<?> list, String title, String sheetName, Class<?> pojoClass, String fileName,
                                   HttpServletResponse response) {
        defaultExport(list, pojoClass, fileName, response, new ExportParams(title, sheetName));
    }

    public static void exportExcel(List<Map<String, Object>> list, String fileName, HttpServletResponse response) {
        defaultExport(list, fileName, response);
    }

    private static void defaultExport(List<?> list, Class<?> pojoClass, String fileName,
                                      HttpServletResponse response, ExportParams exportParams) {
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, pojoClass, list);
        if (workbook != null) ;
        downLoadExcel(fileName, response, workbook);
    }

    private static void bigExcelExport(List<?> data, Class<?> pojoClass, String fileName,
                                      HttpServletResponse response, ExportParams exportParams) {
        int totalPage=(data.size()/10000)+1;
        int pageSize = 10000;
        Workbook workbook = ExcelExportUtil.exportBigExcel(exportParams, pojoClass, new IExcelExportServer() {
            /**
             * obj 就是下面的totalPage，限制条件
             * page 是页数，他是在分页进行文件转换，page每次+1
             */
            @Override
            public List<Object> selectListForExcelExport(Object obj, int page) {
                //page每次加一，当等于obj的值时返回空，代码结束；
                if (page > totalPage) {
                    return null;
                }
                // fromIndex开始索引，toIndex结束索引
                int fromIndex = (page - 1) * pageSize;
                int toIndex = page != totalPage ? fromIndex + pageSize : data.size();

                List<Object> list = new ArrayList<>();
                list.addAll(data.subList(fromIndex, toIndex));
                return list;
            }
        }, totalPage);
        if (workbook != null) ;
        downLoadExcel(fileName, response, workbook);
    }

    public static void downLoadExcel(String fileName, HttpServletResponse response, Workbook workbook) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            //throw new NormalException(e.getMessage());
            log.error("导出EXCEL异常：{}",e);
        }
    }

    private static void defaultExport(List<Map<String, Object>> list, String fileName, HttpServletResponse response) {
        Workbook workbook = ExcelExportUtil.exportExcel(list, ExcelType.HSSF);
        if (workbook != null) ;
        downLoadExcel(fileName, response, workbook);
    }

    public static <T> List<T> importExcel(String filePath, Integer titleRows, Integer headerRows, Class<T> pojoClass) {
        if (StringUtils.isBlank(filePath)) {
            return null;
        }
        ImportParams params = new ImportParams();
        params.setTitleRows(titleRows);
        params.setHeadRows(headerRows);
        List<T> list = null;
        try {
            list = ExcelImportUtil.importExcel(new File(filePath), pojoClass, params);
        } catch (NoSuchElementException e) {
            //throw new NormalException("模板不能为空");
        } catch (Exception e) {
            e.printStackTrace();
            //throw new NormalException(e.getMessage());
        }
        return list;
    }

    public static <T> List<T> importExcel(MultipartFile file, Integer titleRows, Integer headerRows, Class<T> pojoClass) {
        if (file == null) {
            return null;
        }
        ImportParams params = new ImportParams();
        params.setTitleRows(titleRows);
        params.setHeadRows(headerRows);
        List<T> list = null;
        try {
            list = ExcelImportUtil.importExcel(file.getInputStream(), pojoClass, params);
        } catch (NoSuchElementException e) {
            // throw new NormalException("excel文件不能为空");
        } catch (Exception e) {
            //throw new NormalException(e.getMessage());
            System.out.println(e.getMessage());
        }
        return list;
    }

    public static Workbook createWorkbook(String name,List<String>keys,List<String>names,List<Map<String,Object>>data){
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 设置第一个sheet的名称
        HSSFSheet sheet = workbook.createSheet(name);

        // 开始添加excel第一行表头（excel中下标是0）
        HSSFRow row = sheet.createRow(0);
        // 设置excel宽度和高度
        sheet.setDefaultRowHeight((short) (3 * 256));
        sheet.setDefaultColumnWidth(25);

        // 添加excel第一行表头信息（你想要添加的表头数据，集合类型，遍历放进去）
        for (int i = 0; i < keys.size(); i++) {
            // 创建一个单元格
            HSSFCell cell = row.createCell(i);
            // 设置单元格的样式，工具类在下面代码中会有放
            cell.setCellStyle(getRow1CellStyle(workbook));
            // 将数据放入excel的单元格中
            HSSFRichTextString text = new HSSFRichTextString(String.valueOf(names.get(i)));
            cell.setCellValue(text);
        }

        // 开始创建excel单元格数据，从第二行开始（excel下标是1）
        int rowNum = 1;
        // 添加excel行数据的集合（你自己的数据集合遍历）
        for (Map<String, Object> map : data) {
            // 创建一个单元格
            HSSFRow row1 = sheet.createRow(rowNum);
            // 设置行的高度
            row1.setHeightInPoints(20);
            // 遍历你的表头数据，根据表头的数据获取对应的数据(我的数据是map的key,value形式)
            for (int i = 0; i < keys.size(); i++) {
                // 放入单元格中
                String cellValue=String.valueOf(map.get(keys.get(i)));
                if(cellValue==null||"null".equals(cellValue))cellValue="";
                row1.createCell(i).setCellValue(cellValue);
            }
            // 加一行，继续循环添加
            rowNum++;
        }
        return workbook;
    }

    public static Workbook createSchedulingWorkbook(String name, List<String>keys, List<String>names, HashMap<String, Object> weekData, List<Map<String,Object>>data, String month){
        int rowNum = 0;
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 设置第一个sheet的名称
        HSSFSheet sheet = workbook.createSheet(name);

        HSSFRow titleRow = sheet.createRow(rowNum);
        HSSFCell titleRowCell = titleRow.createCell(0);
        HSSFRichTextString titleTest = new HSSFRichTextString(String.format("接龙镇中心卫生院%s排班表",month));
        titleRowCell.setCellValue(titleTest);
        titleRowCell.setCellStyle(getTitleStyle(workbook));
        // 合并A1:A2单元格
        CellRangeAddress titleRegion = new CellRangeAddress(0, 0, 0, keys.size()-1);
        sheet.addMergedRegion(titleRegion);
        rowNum++;
        //标题--------------------------------------------------------------------------------------------
        // 开始添加excel第一行表头（excel中下标是1）
        HSSFRow row = sheet.createRow(rowNum);
        // 设置excel宽度和高度
        sheet.setDefaultRowHeight((short) (3 * 256));
        sheet.setDefaultColumnWidth(15);

        // 添加excel第一行表头信息（你想要添加的表头数据，集合类型，遍历放进去）
        for (int i = 0; i < keys.size(); i++) {
            // 创建一个单元格
            HSSFCell cell = row.createCell(i);
            // 设置单元格的样式，工具类在下面代码中会有放
            cell.setCellStyle(getRow1CellStyle(workbook));
            // 将数据放入excel的单元格中
            HSSFRichTextString text = new HSSFRichTextString(String.valueOf(names.get(i)));
            cell.setCellValue(text);
        }
        //周----------------------------------------------------------------------------------------------
        // 加一行，继续循环添加
        rowNum++;
        // 创建一个单元格
        HSSFRow row1 = sheet.createRow(rowNum);
        // 设置行的高度
        row1.setHeightInPoints(20);
        // 遍历你的表头数据，根据表头的数据获取对应的数据(我的数据是map的key,value形式)
        for (int i = 0; i < keys.size(); i++) {
            // 放入单元格中
            String cellValue=String.valueOf(weekData.get(keys.get(i)));
            if(cellValue==null||"null".equals(cellValue))cellValue="";
            HSSFCell cell = row1.createCell(i);
            cell.setCellStyle(getRow1CellStyle(workbook));
            cell.setCellValue(cellValue);
        }
        // 合并A1:A2单元格
        CellRangeAddress region = new CellRangeAddress(1, 2, 0, 0);
        sheet.addMergedRegion(region);
        //数据----------------------------------------------------------------------------------------------
        // 加一行，继续循环添加
        rowNum++;
        // 开始创建excel单元格数据，从第二行开始（excel下标是1）
        // 添加excel行数据的集合（你自己的数据集合遍历）
        for (Map<String, Object> map : data) {
            // 创建一个单元格
            HSSFRow hssfRow = sheet.createRow(rowNum);
            // 设置行的高度
            hssfRow.setHeightInPoints(20);
            // 遍历你的表头数据，根据表头的数据获取对应的数据(我的数据是map的key,value形式)
            for (int i = 0; i < keys.size(); i++) {
                // 放入单元格中
                String cellValue=String.valueOf(map.get(keys.get(i)));
                if(cellValue==null||"null".equals(cellValue))cellValue="";
                HSSFCell cell = hssfRow.createCell(i);
                cell.setCellValue(cellValue);
                cell.setCellStyle(getStyle(workbook));
            }
            // 加一行，继续循环添加
            rowNum++;
        }
        return workbook;
    }
    public static CellStyle getTitleStyle(Workbook workbook) {

        CellStyle cellStyle = workbook.createCellStyle();
        //设置水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置下边框
        cellStyle.setBorderBottom(BorderStyle.THIN);
        //设置上边框
        cellStyle.setBorderTop(BorderStyle.THIN);
        //设置走边框
        cellStyle.setBorderLeft(BorderStyle.THIN);
        //设置右边框
        cellStyle.setBorderRight(BorderStyle.THIN);
        //设置字体
        Font font = workbook.createFont();
        //设置字号
        font.setFontHeightInPoints((short) 24);
        //设置是否为斜体
        font.setItalic(false);
        //设置是否加粗
        font.setBold(false);
        //设置字体颜色
        font.setColor(IndexedColors.BLACK.index);
        cellStyle.setFont(font);
        //设置背景
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        return cellStyle;
    }
    public static CellStyle getRow1CellStyle(Workbook workbook) {

        CellStyle cellStyle = workbook.createCellStyle();
        //设置水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置下边框
        cellStyle.setBorderBottom(BorderStyle.THIN);
        //设置上边框
        cellStyle.setBorderTop(BorderStyle.THIN);
        //设置走边框
        cellStyle.setBorderLeft(BorderStyle.THIN);
        //设置右边框
        cellStyle.setBorderRight(BorderStyle.THIN);
        //设置字体
        Font font = workbook.createFont();
        //设置字号
//        font.setFontHeightInPoints((short) 14);
        //设置是否为斜体
        font.setItalic(false);
        //设置是否加粗
        font.setBold(false);
        //设置字体颜色
        font.setColor(IndexedColors.BLACK.index);
        cellStyle.setFont(font);
        //设置背景
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(IndexedColors.YELLOW.index);
        return cellStyle;
    }
    public static HSSFCellStyle getStyle(HSSFWorkbook wb){
        HSSFCellStyle style = wb.createCellStyle();
        // 设置字体
        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 10); //字体高度
        font.setColor(HSSFFont.COLOR_NORMAL); //字体颜色
        //设置样式
        style.setFont(font);
        style.setFillBackgroundColor(IndexedColors.TEAL.index);
        style.setAlignment(HorizontalAlignment.CENTER); //水平布局：居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //style.setWrapText(true);
        //设置单元格上下左右边框线
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    public static XSSFCellStyle getStyle(XSSFWorkbook wb){
        XSSFCellStyle style = wb.createCellStyle();
        // 设置字体
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 10); //字体高度
        font.setColor(HSSFFont.COLOR_NORMAL); //字体颜色
        //设置样式
        style.setFont(font);
        style.setFillBackgroundColor(IndexedColors.TEAL.index);
        style.setAlignment(HorizontalAlignment.CENTER); //水平布局：居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //style.setWrapText(true);
        //设置单元格上下左右边框线
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static <T> void handleField(T t, String value, Field field) throws Exception {
        Class<?> type = field.getType();
        if (type == null || type == void.class || StringUtils.isBlank(value)) {
            return;
        }
        if (type == Object.class) {
            field.set(t, value);
            //数字类型
        } else if (type.getSuperclass() == null || type.getSuperclass() == Number.class) {
            if (type == int.class || type == Integer.class) {
                field.set(t, NumberUtils.toInt(value));
            } else if (type == long.class || type == Long.class) {
                field.set(t, NumberUtils.toLong(value));
            } else if (type == byte.class || type == Byte.class) {
                field.set(t, NumberUtils.toByte(value));
            } else if (type == short.class || type == Short.class) {
                field.set(t, NumberUtils.toShort(value));
            } else if (type == double.class || type == Double.class) {
                field.set(t, NumberUtils.toDouble(value));
            } else if (type == float.class || type == Float.class) {
                field.set(t, NumberUtils.toFloat(value));
            } else if (type == char.class || type == Character.class) {
                field.set(t, CharUtils.toChar(value));
            } else if (type == boolean.class) {
                field.set(t, BooleanUtils.toBoolean(value));
            } else if (type == BigDecimal.class) {
                field.set(t, new BigDecimal(value));
            }
        } else if (type == Boolean.class) {
            field.set(t, BooleanUtils.toBoolean(value));
        } else if (type == Date.class) {
            //
            field.set(t, value);
        } else if (type == String.class) {
            field.set(t, value);
        } else {
            Constructor<?> constructor = type.getConstructor(String.class);
            field.set(t, constructor.newInstance(value));
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                return HSSFDateUtil.getJavaDate(cell.getNumericCellValue()).toString();
            } else {
                return new BigDecimal(cell.getNumericCellValue()).toString();
            }
        } else if (cell.getCellType() == CellType.STRING) {
            return StringUtils.trimToEmpty(cell.getStringCellValue());
        } else if (cell.getCellType() == CellType.FORMULA) {
            return StringUtils.trimToEmpty(cell.getCellFormula());
        } else if (cell.getCellType() == CellType.BLANK) {
            return "";
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == CellType.ERROR) {
            return "ERROR";
        } else {
            return cell.toString().trim();
        }

    }

    /**
     * 前提：大数据量下
     * 合并单元格(此方法取消官方很多验证，合并量少不建议使用此方法)
     * @param workbooks
     * @param cra
     */
    public static void addMergeRegion(Workbook workbooks, CellRangeAddress cra) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
//        XSSFSheet sheet1=null;
        if (workbooks instanceof SXSSFWorkbook){
            workbook = (SXSSFWorkbook) workbooks;
        }
        SXSSFSheet sheetAt = workbook.getSheetAt(0);
        CTWorksheet ctWorksheet = sheetAt.getWorkbook().getXSSFWorkbook().getSheetAt(0).getCTWorksheet();

        CTMergeCells ctMergeCells;
        if (ctWorksheet.isSetMergeCells()) {
            ctMergeCells = ctWorksheet.getMergeCells();
        } else {
            ctMergeCells = ctWorksheet.addNewMergeCells();
        }
        CTMergeCell ctMergeCell = ctMergeCells.addNewMergeCell();

        ctMergeCell.setRef(cra.formatAsString());
    }
}
