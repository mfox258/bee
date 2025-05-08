package com.mindskip.xzs.utils;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;

public class ChartUtil {

    // 设置图表主题，解决中文乱码
    public static void setChartTheme() {
        org.jfree.chart.StandardChartTheme standardChartTheme = new org.jfree.chart.StandardChartTheme("CN");
        standardChartTheme.setExtraLargeFont(new Font("宋体", Font.BOLD, 20));
        standardChartTheme.setLargeFont(new Font("宋体", Font.BOLD, 15));
        standardChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 12));
        ChartFactory.setChartTheme(standardChartTheme);
    }

    // 创建图表
    public static JFreeChart createChart(String chartTitle, String xAxisLabel, String yAxisLabel,
                                         DefaultCategoryDataset barDataset, DefaultCategoryDataset lineDataset) {
        setChartTheme();
        // 创建柱状图
        JFreeChart chart = ChartFactory.createBarChart(
                null,
                xAxisLabel,
                yAxisLabel,
                barDataset
        );
        chart.setTitle(chartTitle);

        CategoryPlot plot = chart.getCategoryPlot();

        // 添加折线图数据集
        plot.setDataset(1, lineDataset);

        // 设置柱状图渲染器
        BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
        barRenderer.setBarPainter(new StandardBarPainter());

        // 设置折线图渲染器
        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setSeriesPaint(0, Color.GRAY); // 国内均值
        lineRenderer.setSeriesPaint(1, Color.BLUE); // 全球均值
        lineRenderer.setSeriesStroke(0, new BasicStroke(1.5f));
        lineRenderer.setSeriesStroke(1, new BasicStroke(1.5f));
        lineRenderer.setSeriesShape(1, new Ellipse2D.Double(-3, -3, 6, 6)); // 设置全球均值折线点形状为圆形
        plot.setRenderer(1, lineRenderer);

        // 设置两个Y轴
        NumberAxis leftAxis = (NumberAxis) plot.getRangeAxis(); // 获取左边默认的数值轴
        NumberAxis rightAxis = new NumberAxis("达成率"); // 创建右边的数值轴
        plot.setRangeAxis(1, rightAxis);
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);

        // 设置渲染顺序
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        return chart;
    }

    public static void main(String[] args) {
        setChartTheme();

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

        JFreeChart chart = createChart("测试", "季度", "销售额", barDataset, null);

        // 保存图表为图片
        try {
            int width = 400;
            int height = 300;
            File chartFile = new File("/data/file/car_sales_chart.png");
            ChartUtils.saveChartAsPNG(chartFile, chart, width, height);
            System.out.println("图表已保存为 car_sales_chart.png");
        } catch (IOException e) {
            System.err.println("保存图表时出错: " + e.getMessage());
        }

        // 显示图表窗口（可选）
        ChartFrame frame = new ChartFrame("汽车销售额图表", chart);
        frame.pack();
        frame.setVisible(true);
    }
}