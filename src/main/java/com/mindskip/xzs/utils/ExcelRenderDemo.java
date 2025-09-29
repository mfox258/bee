package com.mindskip.xzs.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class ExcelRenderDemo {

    public static void main(String[] args) {
        String templatePath = "D:\\工程\\mfox\\bee\\src\\main\\resources\\template\\attendance.xlsx";
        String outputPath = "D:\\工程\\mfox\\bee\\src\\main\\resources\\template\\attendance_out.xlsx";
        int year = 2025;
        int month = 8;
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        // 定义C8:AG8和C9:AG9需要填充的值
        String[] row8Values = {"/", "休", "休", "/", "/", "/", "/", "休", "/", "/", "/", "/", "/", "/", "/", "休", "休", "/", "/", "/", "/", "/", "休", "休", "10", "/", "/", "/", "/", "/", "休"};
        String[] row9Values = {"/", "休", "休", "/", "/", "/", "/", "休", "/", "休", "/", "/", "/", "/", "/", "休", "休", "/", "/", "/", "/", "/", "休", "休", "/", "/", "/", "/", "/", "休", "休"};

        String[] weekdays = {"一", "二", "三", "四", "五", "六", "日"};
        // 定义当前日期格式化器（格式：yyyy.MM.dd）
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        // 获取当前系统日期（LocalDate.now()）
        String currentDateStr = LocalDate.now().format(dateFormatter);

        try (FileInputStream fis = new FileInputStream(templatePath);
             Workbook workbook = new XSSFWorkbook(fis);
             FileOutputStream fos = new FileOutputStream(outputPath)) {

            Sheet sheet = workbook.getSheetAt(0);

            // -------------------------- 新增：填充AC2、AG2、AS2 --------------------------
            // 1. 获取第2行（行索引1，Excel行从0开始），不存在则创建
            Row row2 = sheet.getRow(1);
            if (row2 == null) {
                row2 = sheet.createRow(1);
            }

            // 2. 填充AC2：列索引=38（A=0, B=1... AC是第38列），值为year（2025）
            int ac2ColIndex = 28; // AC列对应的索引
            Cell ac2Cell = row2.getCell(ac2ColIndex);
            if (ac2Cell == null) {
                ac2Cell = row2.createCell(ac2ColIndex);
            }
            ac2Cell.setCellValue(year); // 填充年份

            // 3. 填充AG2：列索引=42（AG是第42列），值为month（8）
            int ag2ColIndex = 32; // AG列对应的索引
            Cell ag2Cell = row2.getCell(ag2ColIndex);
            if (ag2Cell == null) {
                ag2Cell = row2.createCell(ag2ColIndex);
            }
            ag2Cell.setCellValue(month); // 填充月份

            // 4. 填充AS2：列索引=44（AS是第44列），值为当前日期（格式yyyy.MM.dd）
            int as2ColIndex = 44; // AS列对应的索引
            Cell as2Cell = row2.getCell(as2ColIndex);
            if (as2Cell == null) {
                as2Cell = row2.createCell(as2ColIndex);
            }
            as2Cell.setCellValue(currentDateStr); // 填充格式化后的当前日期
            // -----------------------------------------------------------------------------

            // 1. 处理第6行：填充日期
            Row row6 = sheet.getRow(5);
            if (row6 == null) row6 = sheet.createRow(5);
            for (int colIndex = 2; colIndex <= 32; colIndex++) {
                int day = colIndex - 1;
                Cell cell = row6.getCell(colIndex);
                if (cell == null) cell = row6.createCell(colIndex);

                if (day <= daysInMonth) {
                    cell.setCellValue(day);
                } else {
                    cell.setCellValue("");
                }
            }

            // 2. 处理第7行：填充星期
            Row row7 = sheet.getRow(6);
            if (row7 == null) row7 = sheet.createRow(6);
            for (int colIndex = 2; colIndex <= 32; colIndex++) {
                int dayOfMonth = colIndex - 1;
                Cell cell = row7.getCell(colIndex);
                if (cell == null) cell = row7.createCell(colIndex);

                if (dayOfMonth <= daysInMonth) {
                    LocalDate date = LocalDate.of(year, month, dayOfMonth);
                    DayOfWeek dayOfWeek = date.getDayOfWeek();
                    int weekIndex = dayOfWeek.getValue() - 1;
                    cell.setCellValue(weekdays[weekIndex]);
                } else {
                    cell.setCellValue("");
                }
            }

            buildContent(daysInMonth, row8Values, row9Values, sheet,0,"张三");

            // 关键：刷新所有公式单元格（强制重新计算）
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll(); // 刷新整个工作簿的所有公式

            workbook.write(fos);
            System.out.println(year + "年" + month + "月数据填充完成（公式已刷新，AC2/AG2/AS2已填充）");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建Excel内容
     * @param daysInMonth 月份天数
     * @param row8Values 第8行值数组
     * @param row9Values 第9行值数组
     * @param sheet Excel工作表
     */
    private static void buildContent(int daysInMonth, String[] row8Values, String[] row9Values, Sheet sheet, int rowIndex,String userName) {
        // 3. 处理第1行
        Row row1 = sheet.getRow(rowIndex+7);
        if (row1 == null) row1 = sheet.createRow(rowIndex+7);
        Cell b8Cell = row1.getCell(1);
        if (b8Cell == null) b8Cell = row1.createCell(1);
        b8Cell.setCellValue(userName);
        for (int colIndex = 2; colIndex <= 32; colIndex++) {
            Cell cell = row1.getCell(colIndex);
            if (cell == null) cell = row1.createCell(colIndex);
            int valueIndex = colIndex - 2;
            if (valueIndex < row8Values.length && (colIndex - 1) <= daysInMonth) {
                cell.setCellValue(row8Values[valueIndex]);
            } else {
                cell.setCellValue("");
            }
        }

        // 4. 处理第2行
        Row row2 = sheet.getRow(rowIndex+8);
        if (row2 == null) row2 = sheet.createRow(rowIndex+8);
        for (int colIndex = 2; colIndex <= 32; colIndex++) {
            Cell cell = row2.getCell(colIndex);
            if (cell == null) cell = row2.createCell(colIndex);
            int valueIndex = colIndex - 2;
            if (valueIndex < row9Values.length && (colIndex - 1) <= daysInMonth) {
                cell.setCellValue(row9Values[valueIndex]);
            } else {
                cell.setCellValue("");
            }
        }
    }
}