package com.mindskip.xzs.utils;

import com.alibaba.fastjson.JSON;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.*;
import com.google.gson.JsonParser;
import com.lark.oapi.Client;
import com.lark.oapi.core.request.RequestOptions;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.drive.v1.model.UploadAllFileReq;
import com.lark.oapi.service.drive.v1.model.UploadAllFileReqBody;
import com.lark.oapi.service.drive.v1.model.UploadAllFileResp;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xwpf.usermodel.*;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 17279
 */
public class PoiUtuls {

    /**
     * @param templateData 生成模板文件所需的所有数据
     * @param templateFilePath 模板文件路径（这里读取的是 resources 下的文件）
     * @param outputFilePath 模板文件输入的地址
     */
    public static void generateWordFile(Map<String, Object> templateData, String templateFilePath, String outputFilePath) {
        // 读取模板文件
        try (InputStream templateIn = PoiUtuls.class.getResourceAsStream(templateFilePath)) {
            // 生成模板文件
            XWPFTemplate template = XWPFTemplate.compile(templateIn).render(templateData);
            XWPFDocument document = template.getXWPFDocument();
            // 设置所有表格文字居中
            for (XWPFTable table : document.getTables()) {
                setTableTextCenter(table);
            }
            template.writeAndClose(new FileOutputStream(outputFilePath));
            // 这个目的是：生成文件之后调用 cmd 打开本地文件，实际生产不需要该操作
            // Runtime.getRuntime().exec(String.format("cmd /c %s", outputFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void setTableTextCenter(XWPFTable table) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                cell.getParagraphs().get(0).setAlignment(ParagraphAlignment.CENTER);
                cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        // 模板文件
        String templateFilePath = "/template/template.docx";
// 输出文件
        String outputFilePath = "D:\\工程\\mfox\\bee\\src\\main\\resources\\template\\new_template.docx";
// 插入文本数据
        Map<String, Object> templateData = new HashMap<String, Object>() {{
            // 添加单系列图表的表格数据
            put("label3", Charts
                    .ofMultiSeries("销售额", new String[]{"第一季度", "第二季度", "第三季度", "第四季度"})
                    .addSeries("电器类", new Integer[]{22, 25, 28, 25})
                    .addSeries("数码类", new Integer[]{5, 10, 8, 4})
                    .addSeries("其他", new Integer[]{30, 42, 22, 33})
                    .addSeries("测试", new Integer[]{30, 42, 22, 33})
                    .create());
            // 添加单系列图表的表格数据
            put("label2", Charts
                    .ofComboSeries("汽车销售额", new String[]{"第一季度", "第二季度", "第三季度", "第四季度"})
                    // 添加柱状图数据
                    .addBarSeries("比亚迪", new Double[]{12.3, 11.5, 9.7, 12.0})
                    .addBarSeries("广汽", new Double[]{6.2, 5.8, 5.7, 6.6})
                    .addBarSeries("小米", new Double[]{0.0, 0.0, 10.2, 11.2})
                    // 添加折线图数据
                    .addLineSeries("国内均值", new Double[]{10.0, 12.2, 11.2, 9.8})
                    .addLineSeries("全球均值", new Double[]{8.3, 10.2, 10.0, 8.8})
                    .create());
            // 添加单系列图表的表格数据
            put("title", "我是标题");


            // 合并单元格
            String[][] table = {
                    new String[]{"姓名", "性别", "年龄"},
                    new String[]{"派大星", "16", "男"},
                    new String[]{"派大星", "18", "男"},
                    new String[]{"派大星", "17", "男"},
                    new String[]{"章鱼哥", "35", "男"},
                    new String[]{"共2人", null, null},
            };
            put("table", Tables.of(table)
                    // 添加单元格合并规则
                    .mergeRule(MergeCellRule
                            .builder()
                            .map(MergeCellRule.Grid.of(1, 0), MergeCellRule.Grid.of(3, 0))
                            // [纵坐标, 横坐标] 索引从零开始，合并 [3, 0] 到 [3, 2] 位置的表格
                            .map(MergeCellRule.Grid.of(5, 0), MergeCellRule.Grid.of(5, 2))

                            .build()
                    )
                    .create()
            );
        }};
        //图表
        // 创建柱状图数据集
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        // 比亚迪销售额
        barDataset.addValue(12, "比亚迪", "第一季度");
        barDataset.addValue(11, "比亚迪", "第二季度");
        barDataset.addValue(10, "比亚迪", "第三季度");
        barDataset.addValue(12, "比亚迪", "第四季度");
        // 广汽销售额
        barDataset.addValue(6, "广汽", "第一季度");
        barDataset.addValue(6, "广汽", "第二季度");
        barDataset.addValue(6, "广汽", "第三季度");
        barDataset.addValue(6, "广汽", "第四季度");
        // 小米销售额
        barDataset.addValue(0, "小米", "第一季度");
        barDataset.addValue(0, "小米", "第二季度");
        barDataset.addValue(10, "小米", "第三季度");
        barDataset.addValue(11, "小米", "第四季度");

        // 创建折线图数据集
        DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();
        // 国内均值
        lineDataset.addValue(1.0, "国内均值", "第一季度");
        lineDataset.addValue(0.5, "国内均值", "第二季度");
        lineDataset.addValue(0.4, "国内均值", "第三季度");
        lineDataset.addValue(0.7, "国内均值", "第四季度");
        // 全球均值
        lineDataset.addValue(0.8, "全球均值", "第一季度");
        lineDataset.addValue(0.10, "全球均值", "第二季度");
        lineDataset.addValue(0.10, "全球均值", "第三季度");
        lineDataset.addValue(0.9, "全球均值", "第四季度");

        JFreeChart chart = ChartUtil.createChart("测试", "季度", "销售额", barDataset, lineDataset);

        // 保存图表为图片
        int width = 400;
        int height = 300;
        File chartFile = new File("/data/file/car_sales_chart.png");
        ChartUtils.saveChartAsPNG(chartFile, chart, width, height);
        System.out.println("图表已保存为 car_sales_chart.png");

        // 显示图表窗口（可选）
        ChartFrame frame = new ChartFrame("汽车销售额图表", chart);
        frame.pack();
        frame.setVisible(true);
        templateData.put("image1", Pictures.ofLocal("/data/file/car_sales_chart.png").size(260, 150).create());
        templateData.put("image2", Pictures.ofLocal("/data/file/car_sales_chart.png").size(260, 150).create());

// 文件生成
        PoiUtuls.generateWordFile(templateData, templateFilePath, outputFilePath);

        // 构建client
        Client client = Client.newBuilder("cli_a768992a9fbc100d", "h0xDh97qy7MRTRgCyOFsvnX3gxPaTG3J").build();
        File file = new File(outputFilePath);
        // 创建请求对象
        UploadAllFileReq req = UploadAllFileReq.newBuilder()
                .uploadAllFileReqBody(UploadAllFileReqBody.newBuilder()
                        .fileName("测试文档0427.docx")
                        .parentType("explorer")
                        .parentNode("InP5f95MEls0yqd46sec2BcRndf")
                        .size((int)file.length())
                        .file(file)
                        .build())
                .build();

        // 发起请求
//        UploadAllFileResp resp = client.drive().v1().file().uploadAll(req);
//
//        // 处理服务端错误
//        if(!resp.success()) {
//            System.out.println(String.format("code:%s,msg:%s,reqId:%s, resp:%s",
//                    resp.getCode(), resp.getMsg(), resp.getRequestId(), Jsons.createGSON(true, false).toJson(JSON.toJSONString(new String(resp.getRawResponse().getBody(), StandardCharsets.UTF_8)))));
//            return;
//        }
    }
}
