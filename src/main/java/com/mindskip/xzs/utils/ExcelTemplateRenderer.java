package com.mindskip.xzs.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * Excel模板多sheet数据渲染工具类
 * 支持读取Excel模板文件的多个sheet页，填充数据后生成新Excel
 */
public class ExcelTemplateRenderer {

    // 模板文件路径
    private final String templatePath;
    // 输出文件路径
    private final String outputPath;
    // 单元格样式缓存（避免重复创建）
    private final Map<String, CellStyle> styleCache = new HashMap<>();
    // 表头行数（默认1行）
    private final int headerRows;

    /**
     * 构造函数（默认1行表头）
     * @param templatePath Excel模板文件路径
     * @param outputPath 渲染后Excel输出路径
     */
    public ExcelTemplateRenderer(String templatePath, String outputPath) {
        this(templatePath, outputPath, 1); // 委托给三参数构造函数，传入默认表头行数1
    }

    /**
     * 构造函数
     * @param templatePath Excel模板文件路径
     * @param outputPath 渲染后Excel输出路径
     * @param headerRows 表头行数（支持多表头行）
     */
    public ExcelTemplateRenderer(String templatePath, String outputPath, int headerRows) {
        this.templatePath = templatePath;
        this.outputPath = outputPath;
        // 验证表头行数
        if (headerRows < 1) {
            throw new IllegalArgumentException("表头行数必须大于等于1");
        }
        this.headerRows = headerRows; // 仅在此处赋值一次
        // 验证模板文件是否存在
        File templateFile = new File(templatePath);
        if (!templateFile.exists() || !templateFile.isFile()) {
            throw new IllegalArgumentException("模板文件不存在或不是有效文件: " + templatePath);
        }
    }



    /**
     * 核心渲染方法
     * @param sheetDataMap key:sheet名称 value:该sheet需要填充的数据列表
     * @throws IOException 读写文件异常
     */
    public void renderExcel(Map<String, List<Map<String, Object>>> sheetDataMap) throws IOException {
        try (InputStream inputStream = new FileInputStream(templatePath);
             Workbook workbook = new XSSFWorkbook(inputStream);
             OutputStream outputStream = new FileOutputStream(outputPath)) {

            // 遍历模板中的所有sheet页
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                // 检查该sheet是否有对应的数据需要填充
                if (sheetDataMap.containsKey(sheetName)) {
                    List<Map<String, Object>> sheetData = sheetDataMap.get(sheetName);
                    // 查找模板中的表头行（默认第一行为表头）
                    Row headerRow = sheet.getRow(headerRows);
                    if (headerRow == null) {
                        throw new RuntimeException("Sheet '" + sheetName + "' 缺少表头行");
                    }

                    // 填充数据到sheet
                    fillSheetData(sheet, headerRow, sheetData);
                } else {
                    System.out.println("Sheet '" + sheetName + "' 无对应数据，保持模板原样");
                }
            }

            // 写入输出文件
            workbook.write(outputStream);
            System.out.println("Excel渲染完成，输出路径: " + outputPath);

        } catch (IOException e) {
            System.err.println("Excel渲染过程中发生IO异常: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 填充单个sheet的数据
     * @param sheet 要填充的sheet
     * @param headerRow 表头行
     * @param dataList 要填充的数据列表
     */
    private void fillSheetData(Sheet sheet, Row headerRow, List<Map<String, Object>> dataList) {
        // 1. 分析表头，建立列名与列索引的映射
        Map<String, List<Integer>> headerMap = new HashMap<>();
        // 获取最后一行表头作为键名行（多表头场景下，以此行作为数据映射的键）
        Row keyHeaderRow = sheet.getRow(headerRows); // headerRows=2时取第1行（0-based）
        if (keyHeaderRow == null) {
            throw new RuntimeException("Sheet '" + sheet.getSheetName() + "' 表头行（第" + headerRows + "行）不存在");
        }
        for (int col = 0; col < headerRow.getLastCellNum(); col++) {
            Cell headerCell = headerRow.getCell(col);
            if (headerCell != null) {
                String originalHeaderName = getCellValueAsString(headerCell).trim();
                // 替换表头中的{{lastMonth}}占位符为实际日期
                String replacedHeaderName = originalHeaderName.replace("{{lastMonth}}", "2025-08");
                // 更新单元格显示值为替换后的名称
                headerCell.setCellValue(replacedHeaderName);
                if (!originalHeaderName.isEmpty()) {
                    // 为重复表头添加多个列索引（使用原始名称作为键，确保数据匹配）
                    headerMap.computeIfAbsent(originalHeaderName, k -> new ArrayList<>()).add(col);
                }
            }
        }

        // 获取表头下方第一行（样式模板行）并保存其样式（表头行数决定样式行位置）
        int styleTemplateRowNum = headerRows; // 表头行：0~headerRows-1，样式行：headerRows
        Row templateStyleRow = sheet.getRow(styleTemplateRowNum);
        if (templateStyleRow == null) {
            throw new RuntimeException("Sheet '" + sheet.getSheetName() + "' 表头下方缺少样式模板行（第" + (styleTemplateRowNum + 1) + "行）");
        }
        Map<Integer, CellStyle> columnStyles = new HashMap<>();
        for (Map.Entry<String, List<Integer>> headerEntry : headerMap.entrySet()) {
            String columnName = headerEntry.getKey();
            List<Integer> columnIndices = headerEntry.getValue();
            for (int columnIndex : columnIndices) {
                Cell styleCell = templateStyleRow.getCell(columnIndex);
                if (styleCell == null) {
                    throw new RuntimeException("Sheet '" + sheet.getSheetName() + "' 样式模板行中列 '" + columnName + "'（索引：" + columnIndex + "）不存在单元格");
                }
                columnStyles.put(columnIndex, styleCell.getCellStyle());
            }
        }

        // 2. 清空原有数据行（保留表头和样式模板行）
        int lastRowNum = sheet.getLastRowNum();
        int dataStartRow = styleTemplateRowNum ; // 数据行起始行（样式行下方）
        if (lastRowNum >= dataStartRow) {
            for (int row = lastRowNum; row >= dataStartRow; row--) {
                sheet.removeRow(sheet.getRow(row));
            }
        }
        // 3. 填充新数据
        Workbook workbook = sheet.getWorkbook();
        for (int dataIdx = 0; dataIdx < dataList.size(); dataIdx++) {
            Map<String, Object> data = dataList.get(dataIdx);
            // 数据行从样式模板行下方开始创建
            Row dataRow = sheet.createRow(dataStartRow + dataIdx);

            // 遍历表头，填充对应列的数据
            for (Map.Entry<String, List<Integer>> headerEntry : headerMap.entrySet()) {
                String columnName = headerEntry.getKey();
                List<Integer> columnIndices = headerEntry.getValue();
                Object cellValue = data.getOrDefault(columnName, ""); // 重复表头的所有列使用相同值

                for (int columnIndex : columnIndices) {
                    // 创建单元格并设置值
                    Cell cell = dataRow.createCell(columnIndex);
                    setCellValue(cell, cellValue);

                    // 设置单元格样式（基于列索引缓存，避免重复表头样式冲突）
                    CellStyle dataCellStyle = columnStyles.get(columnIndex);
                    String styleKey = "col_" + columnIndex + "_" + dataCellStyle.hashCode(); // 使用列索引作为样式键
                    if (!styleCache.containsKey(styleKey)) {
                        CellStyle newCellStyle = workbook.createCellStyle();
                        newCellStyle.cloneStyleFrom(dataCellStyle);
                        styleCache.put(styleKey, newCellStyle);
                    }
                    cell.setCellStyle(styleCache.get(styleKey));
                }
            }
        }
        // 4. 合并第一列相同内容的单元格
        if (dataList.size() > 0 && !headerMap.isEmpty()) {
            // 获取第一列的列名和列索引
            String firstColumnName = "客户ID";
            List<Integer> firstColumnIndices = headerMap.get(firstColumnName);
            if (Objects.nonNull(firstColumnIndices) && !firstColumnIndices.isEmpty()) {
                int firstColumnIndex = firstColumnIndices.get(0); // 取重复表头的第一列进行合并
                int startRow = 0; // 数据列表索引起始值
                Object prevValue = dataList.get(0).get(firstColumnName);

                for (int i = 1; i < dataList.size(); i++) {
                    Object currentValue = dataList.get(i).get(firstColumnName);

                    if (!equals(prevValue, currentValue) || i == dataList.size() - 1) {
                        int endRow = (i == dataList.size() - 1 && equals(prevValue, currentValue)) ? i : i - 1;

                        if (startRow != endRow) {
                            // 合并单元格（Excel行号 = 数据起始行 + 数据列表索引）
                            sheet.addMergedRegion(new CellRangeAddress(
                                    dataStartRow + startRow,  // 起始行（数据起始行 + 列表索引）
                                    dataStartRow + endRow,    // 结束行（数据起始行 + 列表索引）
                                    firstColumnIndex,
                                    firstColumnIndex
                            ));
                        }

                        startRow = i;
                        prevValue = currentValue;
                    }
                }
            }
        }
//        // 5. 自动调整列宽
//        for (int col = 0; col < headerRow.getLastCellNum(); col++) {
//            sheet.autoSizeColumn(col, true);
//        }
    }
    /**
     * 比较两个对象是否相等（处理null情况）
     */
    private boolean equals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * 设置单元格值（根据数据类型自动适配）
     * @param cell 单元格对象
     * @param value 要设置的值
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }

        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            // 设置日期格式（如需自定义格式可修改此处）
            Workbook workbook = cell.getRow().getSheet().getWorkbook();
            CreationHelper creationHelper = workbook.getCreationHelper();
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            cell.setCellStyle(dateStyle);
        } else {
            // 其他类型转为字符串
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 获取单元格的字符串值（处理不同单元格类型）
     * @param cell 单元格对象
     * @return 单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    return cell.getStringCellValue();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
    