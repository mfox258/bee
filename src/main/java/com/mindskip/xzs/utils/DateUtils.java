package com.mindskip.xzs.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateUtils {
    public static List<String> getAllDatesOfMonth(String yearMonthStr) {
        // 定义日期时间格式化器，用于解析输入的年月字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        // 将输入的年月字符串解析为 YearMonth 对象
        YearMonth yearMonth = YearMonth.parse(yearMonthStr, formatter);
        // 获取该月的第一天
        LocalDate firstDay = yearMonth.atDay(1);
        // 获取该月的最后一天
        LocalDate lastDay = yearMonth.atEndOfMonth();
        // 用于存储该月所有日期的列表
        List<String> dates = new ArrayList<>();
        // 定义日期格式化器，用于将日期格式化为 "yyyy-MM-dd"
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 从该月的第一天开始，遍历到最后一天
        for (LocalDate date = firstDay;!date.isAfter(lastDay); date = date.plusDays(1)) {
            // 将日期格式化为 "yyyy-MM-dd" 并添加到列表中
            dates.add(date.format(outputFormatter));
        }
        return dates;
    }

    public static String getDayOfWeek(String dateStr) {
        // 定义日期格式化器，用于解析输入的日期字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 将输入的日期字符串解析为 LocalDate 对象
        LocalDate date = LocalDate.parse(dateStr, formatter);
        // 获取该日期对应的星期几
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        // 返回星期几的名称
        return dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault());
    }

    public static void main(String[] args) {
        System.out.println(getAllDatesOfMonth("2025-02"));
        System.out.println(getDayOfWeek("2025-02-01"));
    }

}
