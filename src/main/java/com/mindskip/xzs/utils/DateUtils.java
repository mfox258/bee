package com.mindskip.xzs.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateUtils {

    // 定义返回结果的key
    public static final String SATURDAY = "saturday";
    public static final String SUNDAY = "sunday";
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



    public static String format(String dateStr) {
        try {
            // 定义输入的日期格式
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // 将字符串解析为LocalDate对象
            LocalDate date = LocalDate.parse(dateStr, inputFormatter);
            // 定义输出的日期格式
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("M月d日");
            // 将LocalDate对象格式化为目标字符串
            return date.format(outputFormatter);
        } catch (Exception e) {
            // 如果解析失败，返回原始字符串或合适的错误提示
            return dateStr;
        }
    }



    /**
     * 获取目标日期的周末（周六和周日），若当前是周末则返回下一周的
     * @param currentDate 当前日期（LocalDate类型）
     * @return 包含周六和周日的Map
     */
    public static Map<String, LocalDate> getWeekend(LocalDate currentDate) {
        Map<String, LocalDate> weekendMap = new HashMap<>(2);
        DayOfWeek currentDay = currentDate.getDayOfWeek();

        LocalDate saturday;
        LocalDate sunday;

        // 判断当前日期是否是周末（周六或周日）
        if (currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY) {
            // 若是周末，获取下一周的周六和周日
            saturday = currentDate.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
            sunday = saturday.plusDays(1);
        } else {
            // 若不是周末，获取本周的周六和周日
            saturday = currentDate.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
            sunday = currentDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        }

        weekendMap.put(SATURDAY, saturday);
        weekendMap.put(SUNDAY, sunday);
        return weekendMap;
    }

    /**
     * 重载方法：直接获取当前系统日期的周末
     * @return 包含周六和周日的Map
     */
    public static Map<String, LocalDate> getCurrentWeekend() {
        return getWeekend(LocalDate.now());
    }

    // 测试方法
    public static void main(String[] args) {
        // 测试1：当前日期为非周末（比如2025-12-20是周六，这里手动指定一个非周末日期）
        LocalDate weekday = LocalDate.of(2025, 12, 18); // 周四
        Map<String, LocalDate> weekdayWeekend = getWeekend(weekday);
        System.out.println("非周末日期(" + weekday + ")的周末：");
        System.out.println("周六：" + weekdayWeekend.get(SATURDAY));
        System.out.println("周日：" + weekdayWeekend.get(SUNDAY));

        // 测试2：当前日期为周六
        LocalDate saturday = LocalDate.of(2025, 12, 20); // 周六
        Map<String, LocalDate> saturdayWeekend = getWeekend(saturday);
        System.out.println("\n周六日期(" + saturday + ")的下一周周末：");
        System.out.println("周六：" + saturdayWeekend.get(SATURDAY));
        System.out.println("周日：" + saturdayWeekend.get(SUNDAY));

        // 测试3：当前系统日期
        Map<String, LocalDate> currentWeekend = getCurrentWeekend();
        System.out.println("\n当前系统日期的周末：");
        System.out.println("周六：" + currentWeekend.get(SATURDAY));
        System.out.println("周日：" + currentWeekend.get(SUNDAY));
    }
}
