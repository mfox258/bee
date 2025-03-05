package com.mindskip.xzs.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.mindskip.xzs.domain.SchedulingInfo;
import com.mindskip.xzs.domain.User;
import com.mindskip.xzs.repository.SchedulingInfoMapper;
import com.mindskip.xzs.repository.UserMapper;
import com.mindskip.xzs.service.SchedulingInfoService;
import com.mindskip.xzs.utils.DateUtils;
import com.mindskip.xzs.utils.ExcelUtils;
import com.mindskip.xzs.viewmodel.scheduling.SchedulingEditRequest;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SchedulingInfoServiceImpl extends ServiceImpl<SchedulingInfoMapper, SchedulingInfo> implements SchedulingInfoService {
    private final UserMapper userMapper;
    @Override
    @Transactional
    public void edit(SchedulingEditRequest request) {
        this.baseMapper.delete(Wrappers.<SchedulingInfo>lambdaQuery().eq(SchedulingInfo::getMonth,request.getMonth()));
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
            names.add(date);
        }

        //4.sheet的datas
        List<Map<String, Object>>  datas = Lists.newArrayList();
        HashMap<String, Object> weekData = new HashMap<>();
        weekData.put("姓名","/");
        for (String date : monthDates) {
            weekData.put(date,DateUtils.getDayOfWeek(date));
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
}
