package com.mindskip.xzs.utils;


import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.Pictures;

public class PoiTlUtils {
    public static void noticePayment(NoticePaymentData data,String resource,String target) throws Exception{
        Configure configure=Configure.builder().bind("detailTable", new DetailTablePolicy()).build();
        XWPFTemplate template = XWPFTemplate.compile(resource, configure).render(data);
        template.writeToFile(target);
    }
//
//    public static void main(String[] args) throws Exception{
//        NoticePaymentData noticePaymentData = new NoticePaymentData();
//        String name="业务查房";
//        noticePaymentData.setName(name);
//        noticePaymentData.setYear("2024");
//        noticePaymentData.setMonth("11");
//        noticePaymentData.setDay("20");
//        String filename="F:\\zyz\\page\\"+name;
//        PictureRenderData pictureRenderData1 = Pictures.ofLocal(filename+".jpg").size(520, 290).create();
//        noticePaymentData.setPictureA(pictureRenderData1);
//        PictureRenderData pictureRenderData2 = Pictures.ofLocal(filename+"1.jpg").size(520, 290).create();
//        noticePaymentData.setPictureB(pictureRenderData2);
//
//        String resource="F:\\zyz\\模板.docx";
//        String target="F:\\zyz\\test\\"+name+".docx";
//        PoiTlUtils.noticePayment(noticePaymentData,resource,target);
//    }
}
