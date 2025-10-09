package com.mindskip.xzs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.lark.oapi.core.utils.Sets;
import com.mindskip.xzs.domain.*;
import com.mindskip.xzs.repository.ClassesAttendanceMappingMapper;
import com.mindskip.xzs.repository.ClassesStatisticRuleMapper;
import com.mindskip.xzs.repository.SchedulingInfoMapper;
import com.mindskip.xzs.repository.UserMapper;
import com.mindskip.xzs.service.ClassesRuleService;
import com.mindskip.xzs.service.ClassesService;
import com.mindskip.xzs.service.SchedulingInfoService;
import com.mindskip.xzs.utils.DateUtils;
import com.mindskip.xzs.utils.ExcelUtils;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingEditRequest;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingStatisticsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulingInfoServiceImpl extends ServiceImpl<SchedulingInfoMapper, SchedulingInfo> implements SchedulingInfoService {
    private final UserMapper userMapper;
    private final ClassesRuleService classesRuleService;
    private final ClassesService classesService;
    private final ClassesStatisticRuleMapper classesStatisticRuleMapper;
    private final ClassesAttendanceMappingMapper classesAttendanceMappingMapper;
    @Override
    @Transactional
    public void edit(SchedulingEditRequest request) {
        this.baseMapper.delete(Wrappers.<SchedulingInfo>lambdaQuery()
                .ge(SchedulingInfo::getMonth,request.getStartMonth())
                .le(SchedulingInfo::getMonth,request.getEndMonth())
                .in(CollectionUtils.isNotEmpty(request.getSchedulingInfos()),SchedulingInfo::getUserName,request.getSchedulingInfos().stream().map(SchedulingInfo::getUserName).collect(Collectors.toList())));
        this.saveBatch(request.getSchedulingInfos());
    }

    @Override
    public Workbook export(String startMonth, String endMonth) {
        List<SchedulingInfo> schedulingInfos = userMapper.list(startMonth, endMonth);
        List<User> allUser = userMapper.getAllUser();
        List<User> guests = allUser.stream().filter(user -> user.getRole() == 1).sorted(Comparator.comparing(User::getUserLevel)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(schedulingInfos)){
            return new HSSFWorkbook();
        }
        //1.sheet名
        String sheetName = "排班表("+startMonth+"-"+endMonth+")";
        //2.sheet的keys
        List<String> keys = Lists.newArrayList();
        keys.add("姓名");
        //2.2日期
        Set<String> uniqueDates = new HashSet<>();
        List<String> monthDates = DateUtils.getAllDatesOfMonth(startMonth);
        List<String> endDates = DateUtils.getAllDatesOfMonth(endMonth);
        monthDates.addAll(endDates);
        for (String date : monthDates) {
            if (uniqueDates.add(date)) {
                keys.add(date);
            }
        }

        //3.sheet的names
        List<String> names = Lists.newArrayList();
        names.add("姓名");
        //2.2日期
        for (String date : monthDates) {
            names.add(DateUtils.format(date));
        }

        //4.sheet的datas
        List<Map<String, Object>>  datas = Lists.newArrayList();
        HashMap<String, Object> weekData = new HashMap<>();
        weekData.put("姓名","/");
        for (String date : monthDates) {
            weekData.put(date,DateUtils.getDayOfWeek(date)+" ");
        }
        Map<String, List<SchedulingInfo>> userSchedulings = schedulingInfos.stream().collect(Collectors.groupingBy(SchedulingInfo::getUserName));
        for (User guest : guests) {//按照排序优先展示用户
            userSchedulings.forEach((userName, list)->{
                if (!Objects.equals(guest.getRealName(),userName))return;
                HashMap<String, Object> userClassesData= new HashMap<>();
                Map<String, String> dateClassesMap = list.stream().filter(schedulingInfo -> Objects.nonNull(schedulingInfo.getDate())).collect(Collectors.toMap(SchedulingInfo::getDate, SchedulingInfo::getClasses, (v1, v2) -> v1));
                userClassesData.put("姓名",userName);
                for (String date : monthDates) {
                    userClassesData.put(date,dateClassesMap.get(date));
                }
                datas.add(userClassesData);
            });
        }

        return ExcelUtils.createSchedulingWorkbook(sheetName,keys,names,weekData,datas,startMonth);
    }

    /**
     * 导出统计信息
     * @param startMonth
     * @param endMonth
     * @return
     */
    @Override
    public Workbook exportStatistics(String startMonth, String endMonth) {
        //查询人员班次统计
        List<SchedulingStatisticsResponse> statisticsResponses = this.statisticsList(startMonth, endMonth);
        //查询需要统计的班次
        List<String> classesList = classesService.selectList(1, null, null);
        //查询员工列表
        List<User> allUser = userMapper.getAllUser();
        List<User> guests = allUser.stream().filter(user -> user.getRole() == 1).sorted(Comparator.comparing(User::getUserLevel)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(statisticsResponses)){
            return new HSSFWorkbook();
        }
        //1.sheet名
        String sheetName = "排班表统计("+startMonth+"-"+endMonth+")";
        //2.sheet的keys
        List<String> keys = Lists.newArrayList();
        keys.add("姓名");
        //2.2班次
        for (String classes : classesList) {
            keys.add(classes);
        }

        //3.sheet的names
        List<String> names = Lists.newArrayList();
        names.add("姓名");
        //2.2日期
        for (String classes : classesList) {
            names.add(classes);
        }

        //4.sheet的datas
        List<Map<String, Object>>  datas = Lists.newArrayList();
        Map<String, List<SchedulingStatisticsResponse>> statisMap = statisticsResponses.stream().collect(Collectors.groupingBy(SchedulingStatisticsResponse::getUserName));
        for (User guest : guests) {//按照排序优先展示用户
            statisMap.forEach((userName, list)->{
                if (!Objects.equals(guest.getRealName(),userName))return;
                HashMap<String, Object> userClassesData= new HashMap<>();
                Map<String, String> userClassesMap = list.stream().collect(Collectors.toMap(SchedulingStatisticsResponse::getClasses, SchedulingStatisticsResponse::getCount, (v1, v2) -> v1));
                userClassesData.put("姓名",userName);
                for (String classes : classesList) {
                    userClassesData.put(classes,userClassesMap.get(classes));
                }
                datas.add(userClassesData);
            });
        }

        return ExcelUtils.createWorkbook(sheetName,keys,names,datas);
    }

    @Override
    public List<SchedulingStatisticsResponse> statisticsList(String startMonth, String endMonth) {
        List<SchedulingStatisticsResponse> statisticsResponses = Lists.newArrayList();
        List<SchedulingInfo> schedulingInfos = userMapper.list(startMonth,endMonth);
        List<String> classes = classesService.selectList(1, null, null);
        List<ClassesRule> classesRules = classesRuleService.selectList();
        Map<String, List<SchedulingInfo>> userSchedulings = schedulingInfos.stream().collect(Collectors.groupingBy(SchedulingInfo::getUserName));
        Map<String, List<ClassesRule>> ruleMap = classesRules.stream().collect(Collectors.groupingBy(ClassesRule::getTargetClasses));
        userSchedulings.forEach((userName,list)->{
            classes.forEach(cl->{
                SchedulingStatisticsResponse statisticsResponse = new SchedulingStatisticsResponse();
                BigDecimal targetClassesNum=BigDecimal.ZERO;
                //本班次+1
                for (SchedulingInfo schedulingInfo : list) {
                    if (Objects.equals(cl,schedulingInfo.getClasses())){
                        targetClassesNum = targetClassesNum.add(new BigDecimal(1));
                        continue;
                    }
                }
                //将班次，次数,姓名 放进schedulingStatisticsResponses
                List<ClassesRule> ruleList = ruleMap.get(cl);
                if (CollectionUtils.isNotEmpty(ruleList)) {
                    //映射的班次加对应系数
                    for (ClassesRule classesRule : ruleList) {
                        for (SchedulingInfo schedulingInfo : list) {
                            if (Objects.equals(classesRule.getClasses(), schedulingInfo.getClasses())) {
                                targetClassesNum = targetClassesNum.add(classesRule.getRatio());
                                continue;
                            }
                        }
                    }
                }

                statisticsResponse.setUserName(userName);
                statisticsResponse.setClasses(cl);
                statisticsResponse.setCount(targetClassesNum.setScale(1).toString());
                statisticsResponses.add(statisticsResponse);
            });


        });

        List<User> allUser = userMapper.getAllUser();
        Map<String, Integer> userLevelMap = allUser.stream().filter(user -> user.getRole() == 1).collect(Collectors.toMap(User::getRealName, User::getUserLevel, (v1, v2) -> v1));
        return statisticsResponses.stream().sorted(Comparator.comparing(o->userLevelMap.get(o.getUserName()))).collect(Collectors.toList());
    }

    @Override
    public List<SchedulingStatisticsResponse> statistic(String startMonth, String endMonth) {
        List<SchedulingStatisticsResponse> responses=new ArrayList<>();

        List<SchedulingStatisticsResponse> statisticsResponses = this.statisticsList(startMonth, endMonth);
        List<ClassesStatisticRule> classesStatisticRules = this.classesStatisticRuleMapper.selectList(Wrappers.<ClassesStatisticRule>lambdaQuery().isNotNull(ClassesStatisticRule::getRatio));
        Map<String, String> classesStatisticRuleMap = classesStatisticRules.stream().collect(Collectors.toMap(ClassesStatisticRule::getClasses, ClassesStatisticRule::getStatisticClasses, (o1, o2) -> o1));
        Map<String, Double> classesStatisticRatioMap = classesStatisticRules.stream().collect(Collectors.toMap(ClassesStatisticRule::getStatisticClasses, ClassesStatisticRule::getRatio, (o1, o2) -> o1));
        Map<String, String> classesStatisticMap = new HashMap<>();
        Map<String, String> sumDayMap = new HashMap<>();
        //查询用户的职级
        List<User> users = userMapper.getActiveUser();
        Map<String, String> userJobRankMap = users.stream().filter(data->StringUtils.isNotEmpty(data.getJobRank())).collect(Collectors.toMap(User::getRealName, User::getJobRank, (o1, o2) -> o1));

        //刷新排班的班次名称
        for (SchedulingStatisticsResponse statisticsRespons : statisticsResponses) {
            statisticsRespons.setClasses(classesStatisticRuleMap.get(statisticsRespons.getClasses()));
        }
        //分组就和
        statisticsResponses.stream().filter(data-> StringUtils.isNotEmpty(data.getClasses())).collect(Collectors.groupingBy(data->data.getUserName()+"-"+data.getClasses())).forEach((classes, list)->{
            String sumCount = classesStatisticMap.get(list.get(0).getUserName());
            if (Objects.isNull(sumCount)){
                sumCount="0";
            }
            String sumDayCount = sumDayMap.get(list.get(0).getUserName());
            if (Objects.isNull(sumDayCount)){
                sumDayCount="0";
            }

            SchedulingStatisticsResponse response = new SchedulingStatisticsResponse();
            response.setClasses(list.get(0).getClasses());
            response.setUserName(list.get(0).getUserName());
            // 将 count 类型改为 BigDecimal
            BigDecimal count = list.stream()
                    .map(data -> new BigDecimal(data.getCount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            sumDayCount = sumAsBigDecimal(count.stripTrailingZeros().toPlainString(), sumDayCount);
            if (count.compareTo(BigDecimal.ZERO) == 0) {
                response.setCount("");
            } else if (Objects.equals(list.get(0).getClasses(), "休假")) {
                response.setCount(count.stripTrailingZeros().toPlainString());
            } else {
                // 将 ratio 类型改为 BigDecimal
                BigDecimal ratio = BigDecimal.valueOf(classesStatisticRatioMap.getOrDefault(list.get(0).getClasses(), 0.0));
                // 调用 multiplyAndRound 方法，传入 BigDecimal 类型参数
                String result = multiplyAndRound(ratio, count);
                sumCount = sumAsBigDecimal(result, sumCount);
                response.setCount(String.format("%s*%s=%s", ratio.stripTrailingZeros().toPlainString(), count.stripTrailingZeros().toPlainString(), result));
            }
            classesStatisticMap.put(list.get(0).getUserName(), sumCount);
            sumDayMap.put(list.get(0).getUserName(), sumDayCount);
            responses.add(response);
        });
        //处理合计
        classesStatisticMap.forEach((userName, sumCount) -> {
            SchedulingStatisticsResponse response = new SchedulingStatisticsResponse();
            response.setUserName(userName);
            response.setClasses("总合计");
            response.setCount(sumCount);
            responses.add(response);
        });
        userJobRankMap.forEach((userName, jobRank) -> {
            SchedulingStatisticsResponse response = new SchedulingStatisticsResponse();
            response.setUserName(userName);
            response.setClasses("职称");
            response.setCount(jobRank);
            responses.add(response);
        });
        sumDayMap.forEach((userName, sumDayCount) -> {
            SchedulingStatisticsResponse response = new SchedulingStatisticsResponse();
            response.setUserName(userName);
            response.setClasses("总天数");
            response.setCount(sumDayCount);
            responses.add(response);
        });

        return responses;
    }

    @Override
    public Workbook exportAttendance(Integer year, Integer month) {
        String templatePath = "/template/attendance.xlsx";
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        List<SchedulingInfo> schedulingInfos = this.baseMapper.selectList(new QueryWrapper<SchedulingInfo>().lambda().eq(SchedulingInfo::getMonth, yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))));
        List<User> guests =  userMapper.getActiveUser();

        List<ClassesAttendanceMapping> classesAttendanceMappings = classesAttendanceMappingMapper.selectList(new QueryWrapper<>());
        Map<String, ClassesAttendanceMapping> classesAttendanceMappingMap = classesAttendanceMappings.stream().collect(Collectors.toMap(ClassesAttendanceMapping::getClasses,data->data,(o1,o2)->o1));
        String[] weekdays = {"一", "二", "三", "四", "五", "六", "日"};
        // 定义当前日期格式化器（格式：yyyy.MM.dd）
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        // 获取当前系统日期（LocalDate.now()）
        String currentDateStr = LocalDate.now().format(dateFormatter);

        try ( // 修复：通过类加载器读取classpath中的模板文件，替换FileInputStream
              InputStream fis = getClass().getResourceAsStream(templatePath);
            ) {
            // 验证资源是否存在
            if (fis == null) {
                log.error("模板文件不存在: {}", templatePath);
                return null;
            }
            // 关键：Workbook不使用try-with-resources，避免自动关闭
            Workbook workbook = new XSSFWorkbook(fis);
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
                    LocalDate localDate1 = LocalDate.of(year, month, dayOfMonth);
                    DayOfWeek dayOfWeek = localDate1.getDayOfWeek();
                    int weekIndex = dayOfWeek.getValue() - 1;
                    cell.setCellValue(weekdays[weekIndex]);
                } else {
                    cell.setCellValue("");
                }
            }
            final int[] rowIndex = {0};
            Map<String, List<SchedulingInfo>> userSchedulingInfos = schedulingInfos.stream().collect(Collectors.groupingBy(SchedulingInfo::getUserName));
            for (User guest : guests) {
                List<SchedulingInfo> list = userSchedulingInfos.getOrDefault(guest.getRealName(), new ArrayList<>());
                if (list.isEmpty()){
                    continue;
                }
                List<String> amValues = new ArrayList<>();
                List<String> pmValues = new ArrayList<>();
                if (!Objects.equals(list.size(), daysInMonth)){
                    throw new RuntimeException("用户"+guest.getRealName()+"在"+yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))+"月的排班信息不足"+daysInMonth+"天");
                }
                list.stream().sorted(Comparator.comparing(SchedulingInfo::getDate)).forEach(schedulingInfo -> {
                    ClassesAttendanceMapping classesAttendanceMapping = classesAttendanceMappingMap.get(schedulingInfo.getClasses());
                    if (classesAttendanceMapping != null) {
                        amValues.add(classesAttendanceMapping.getAttendanceAm());
                        pmValues.add(classesAttendanceMapping.getAttendancePm());
                    }else {
                        amValues.add("/");
                        pmValues.add("/");
                    }
                });

                buildContent(daysInMonth, amValues.toArray(new String[0]), pmValues.toArray(new String[0]), sheet, rowIndex[0],guest.getRealName());
                rowIndex[0]+=2;
            }

            // 关键：刷新所有公式单元格（强制重新计算）
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll(); // 刷新整个工作簿的所有公式
            log.info(year + "年" + month + "月数据填充完成（公式已刷新，AC2/AG2/AS2已填充）");
            return workbook;

        } catch (IOException e) {
            log.error("导出 {} 年 {} 月数据失败", year, month, e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Workbook exportOvertime(Integer year, Integer month) {
        String templatePath = "/template/work_overtime.xlsx";
        YearMonth yearMonth = YearMonth.of(year, month);

        List<SchedulingStatisticsResponse> statisticsResponses = this.statisticsList(yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")), yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        List<User> guests = userMapper.getActiveUser();

        try ( // 修复：通过类加载器读取classpath中的模板文件，替换FileInputStream
              InputStream fis = getClass().getResourceAsStream(templatePath);
        ) {
            // 验证资源是否存在
            if (fis == null) {
                log.error("模板文件不存在: {}", templatePath);
                return null;
            }

            // 关键：Workbook不使用try-with-resources，避免自动关闭
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            Row row2 = sheet.getRow(1);
            if (row2 == null) {
                row2 = sheet.createRow(1);
            }
            Cell cell9 = row2.getCell(8);
            if (cell9 == null) cell9 = row2.createCell(8);
            cell9.setCellValue(yearMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")));
            Map<String, List<SchedulingStatisticsResponse>> userStatisticsResponses = statisticsResponses.stream().collect(Collectors.groupingBy(SchedulingStatisticsResponse::getUserName));
            for (int i = 0; i < guests.size(); i++) {
                User guest = guests.get(i);
                Row row = sheet.getRow(3+i);
                if (row == null) row = sheet.createRow(3+i);
                Cell cell = row.getCell(0);
                if (cell == null) cell = row.createCell(0);
                cell.setCellValue(guest.getRealName());
                List<SchedulingStatisticsResponse> list = userStatisticsResponses.getOrDefault(guest.getRealName(), new ArrayList<>());
                if (list.isEmpty()){
                    list=Collections.emptyList();
                }
                ArrayList<Double> values = new ArrayList<>();
                SchedulingStatisticsResponse night = list.stream().filter(schedulingStatisticsResponse ->
                        Objects.equals(schedulingStatisticsResponse.getClasses(), "夜")).findFirst().orElse(null);
                SchedulingStatisticsResponse amResponse = list.stream().filter(schedulingStatisticsResponse ->
                        Objects.equals(schedulingStatisticsResponse.getClasses(), "中")).findFirst().orElse(null);
                SchedulingStatisticsResponse pmResponse = list.stream().filter(schedulingStatisticsResponse ->
                        Objects.equals(schedulingStatisticsResponse.getClasses(), "120")).findFirst().orElse(null);
                values.add(night != null ? Double.parseDouble(night.getCount()) : 0.0);
                values.add(50d);
                values.add(amResponse != null ? Double.parseDouble(amResponse.getCount()) : 0.0);
                values.add(40d);
                values.add(pmResponse != null ? Double.parseDouble(pmResponse.getCount()) : 0.0);
                values.add(20d);
                values.add(0d);
                values.add(10d);
                for (int colIndex = 1; colIndex <= 9; colIndex++) {
                    Cell cell1 = row.getCell(colIndex);
                    if (cell1 == null) cell1 = row.createCell(colIndex);
                    int valueIndex = colIndex - 1;
                    if (valueIndex < values.size()) {
                        cell1.setCellValue(values.get(valueIndex));

                    } else {
                        cell1.setCellValue("");
                    }
                }
            }

            // 关键：刷新所有公式单元格（强制重新计算）
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll(); // 刷新整个工作簿的所有公式
            return workbook;

        } catch (IOException e) {
            log.error("导出 {} 年 {} 月数据失败", year, month, e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 构建Excel内容
     * @param daysInMonth 月份天数
     * @param amValues 第8行值数组
     * @param pmValues 第9行值数组
     * @param sheet Excel工作表
     */
    private static void buildContent(int daysInMonth, String[] amValues, String[] pmValues, Sheet sheet, int rowIndex,String userName) {
        // 3. 处理第1行
        Row amRow = sheet.getRow(rowIndex+7);
        if (amRow == null) amRow = sheet.createRow(rowIndex+7);
        Cell b8Cell = amRow.getCell(1);
        if (b8Cell == null) b8Cell = amRow.createCell(1);
        b8Cell.setCellValue(userName);
        for (int colIndex = 2; colIndex <= 32; colIndex++) {
            Cell cell = amRow.getCell(colIndex);
            if (cell == null) cell = amRow.createCell(colIndex);
            int valueIndex = colIndex - 2;
            if (valueIndex < amValues.length && (colIndex - 1) <= daysInMonth) {
                cell.setCellValue(amValues[valueIndex]);
            } else {
                cell.setCellValue("");
            }
        }

        // 4. 处理第2行
        Row pmRow = sheet.getRow(rowIndex+8);
        if (pmRow == null) pmRow = sheet.createRow(rowIndex+8);
        for (int colIndex = 2; colIndex <= 32; colIndex++) {
            Cell cell = pmRow.getCell(colIndex);
            if (cell == null) cell = pmRow.createCell(colIndex);
            int valueIndex = colIndex - 2;
            if (valueIndex < pmValues.length && (colIndex - 1) <= daysInMonth) {
                cell.setCellValue(pmValues[valueIndex]);
            } else {
                cell.setCellValue("");
            }
        }
    }

    /**
     * 计算Double与Integer的乘积，并保留2位小数
     * @param bd1 双精度浮点数
     * @param bd2 整数
     * @return 保留两位小数的乘积结果
     */
    public static String multiplyAndRound(BigDecimal bd1, BigDecimal bd2) {
        // 处理 null 值情况
        if (bd1 == null || bd2 == null) {
            throw new IllegalArgumentException("输入参数不能为 null");
        }

        BigDecimal product = bd1.multiply(bd2);

        // 四舍五入保留两位小数
        BigDecimal rounded = product.setScale(2, RoundingMode.HALF_UP);

        // 去掉小数点后的无效 0
        return rounded.stripTrailingZeros().toPlainString();
    }
    /**
     * 将传入的 result 和 sumCount 转换为 BigDecimal 类型后进行求和，结果以字符串形式返回
     * @param result 第一个求和参数，可为字符串表示的数字
     * @param sumCount 第二个求和参数，可为字符串表示的数字
     * @return 求和结果的字符串表示，若参数为 null 或空字符串则视为 0
     */
    public static String sumAsBigDecimal(String result, String sumCount) {
        // 处理 null 或空字符串，将其视为 0
        BigDecimal bdResult = StringUtils.isEmpty(result) ? BigDecimal.ZERO : new BigDecimal(result);
        BigDecimal bdSumCount = StringUtils.isEmpty(sumCount) ? BigDecimal.ZERO : new BigDecimal(sumCount);

        // 进行求和操作
        BigDecimal sum = bdResult.add(bdSumCount);

        // 去掉小数点后的无效 0 并返回字符串
        return sum.stripTrailingZeros().toPlainString();
    }
    public static void main(String[] args) {

    }

}
