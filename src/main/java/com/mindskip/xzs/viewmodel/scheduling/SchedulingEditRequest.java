package com.mindskip.xzs.viewmodel.scheduling;

import com.mindskip.xzs.domain.SchedulingInfo;
import lombok.Data;

import java.util.List;

@Data
public class SchedulingEditRequest {

    private String month;
    List<SchedulingInfo> schedulingInfos;
}
