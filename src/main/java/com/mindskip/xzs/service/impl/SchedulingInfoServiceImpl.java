package com.mindskip.xzs.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.mindskip.xzs.domain.ClassesRule;
import com.mindskip.xzs.domain.SchedulingInfo;
import com.mindskip.xzs.domain.User;
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
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SchedulingInfoServiceImpl extends ServiceImpl<SchedulingInfoMapper, SchedulingInfo> implements SchedulingInfoService {
    private final UserMapper userMapper;
    private final ClassesRuleService classesRuleService;
    private final ClassesService classesService;
    @Override
    @Transactional
    public void edit(SchedulingEditRequest request) {
        this.baseMapper.delete(Wrappers.<SchedulingInfo>lambdaQuery()
                .eq(SchedulingInfo::getMonth,request.getMonth())
                .in(CollectionUtils.isNotEmpty(request.getSchedulingInfos()),SchedulingInfo::getUserName,request.getSchedulingInfos().stream().map(SchedulingInfo::getUserName).collect(Collectors.toList())));
        this.saveBatch(request.getSchedulingInfos());
    }

    @Override
    public Workbook export(String month) {
        List<SchedulingInfo> schedulingInfos = userMapper.list(month);
        List<User> allUser = userMapper.getAllUser();
        List<User> guests = allUser.stream().filter(user -> user.getRole() == 1).sorted(Comparator.comparing(User::getUserLevel)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(schedulingInfos)){
            return new HSSFWorkbook();
        }
        //1.sheet名
        String sheetName = "排班表("+month+")";
        //2.sheet的keys
        List<String> keys = Lists.newArrayList();
        keys.add("姓名");
        //2.2日期
        List<String> monthDates = DateUtils.getAllDatesOfMonth(month);
        for (String date : monthDates) {
            keys.add(date);
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

        return ExcelUtils.createSchedulingWorkbook(sheetName,keys,names,weekData,datas,month);
    }

    /**
     * 导出统计信息
     * @param month
     * @return
     */
    @Override
    public Workbook exportStatistics(String month) {
        //查询人员班次统计
        List<SchedulingStatisticsResponse> statisticsResponses = this.statisticsList(month);
        //查询需要统计的班次
        List<String> classesList = classesService.selectList(1, null, null);
        //查询员工列表
        List<User> allUser = userMapper.getAllUser();
        List<User> guests = allUser.stream().filter(user -> user.getRole() == 1).sorted(Comparator.comparing(User::getUserLevel)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(statisticsResponses)){
            return new HSSFWorkbook();
        }
        //1.sheet名
        String sheetName = "排班表统计("+month+")";
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
    public List<SchedulingStatisticsResponse> statisticsList(String month) {
        List<SchedulingStatisticsResponse> statisticsResponses = Lists.newArrayList();
        List<SchedulingInfo> schedulingInfos = userMapper.list(month);
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
}
