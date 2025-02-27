package com.mindskip.xzs.utils;

import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.TableRenderData;
import com.deepoove.poi.expression.Name;
import lombok.Data;

import java.util.List;

/**
 * 数据模型
 */
@Data
public class NoticePaymentData {
    //名字
    private String name;
    //时间 年
    private String year;
    //时间 月
    private String month;
    //时间 日
    private String day;
    private String location;
    //图片1
    @Name("pictureA")
    private PictureRenderData pictureA;
    //图片2
    @Name("pictureB")
    private PictureRenderData pictureB;
}


