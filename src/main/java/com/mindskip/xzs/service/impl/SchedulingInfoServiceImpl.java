package com.mindskip.xzs.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.lark.oapi.core.utils.Sets;
import com.mindskip.xzs.domain.ClassesRule;
import com.mindskip.xzs.domain.ClassesStatisticRule;
import com.mindskip.xzs.domain.SchedulingInfo;
import com.mindskip.xzs.domain.User;
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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SchedulingInfoServiceImpl extends ServiceImpl<SchedulingInfoMapper, SchedulingInfo> implements SchedulingInfoService {
    private final UserMapper userMapper;
    private final ClassesRuleService classesRuleService;
    private final ClassesService classesService;
    private final ClassesStatisticRuleMapper classesStatisticRuleMapper;
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

            SchedulingStatisticsResponse response = new SchedulingStatisticsResponse();
            response.setClasses(list.get(0).getClasses());
            response.setUserName(list.get(0).getUserName());
            // 将 count 类型改为 BigDecimal
            BigDecimal count = list.stream()
                    .map(data -> new BigDecimal(data.getCount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
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

        return responses;
    }

    /**
     * 计算Double与Integer的乘积，并保留2位小数
     * @param doubleNum 双精度浮点数
     * @param intNum 整数
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
