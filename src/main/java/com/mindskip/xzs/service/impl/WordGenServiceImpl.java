package com.mindskip.xzs.service.impl;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.PictureType;
import com.deepoove.poi.data.Pictures;
import com.mindskip.xzs.service.WordGenService;
import com.mindskip.xzs.utils.DetailTablePolicy;
import com.mindskip.xzs.utils.NoticePaymentData;
import com.mindskip.xzs.utils.PoiTlUtils;
import lombok.SneakyThrows;
import org.apache.http.util.Asserts;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * 文档生成服务层
 */
@Service
public class WordGenServiceImpl  implements WordGenService {

    public static void noticePayment(NoticePaymentData data, String resource, String target) throws Exception{
        Configure configure=Configure.builder().bind("detailTable", new DetailTablePolicy()).build();
        XWPFTemplate template = XWPFTemplate.compile(resource, configure).render(data);
        template.writeToFile(target);
    }

    /**
     * 生成接龙镇卫生院会议记录word
     *
     * @param fileName
     * @param time
     * @param location
     * @param files
     * @param response
     * @return
     */
    @SneakyThrows
    @Override
    public void genWordWithJLMeeting(String fileName, String time, String location, List<MultipartFile> files, HttpServletResponse response) {
        NoticePaymentData noticePaymentData = new NoticePaymentData();
        noticePaymentData.setName(fileName);
        String[] date = time.split("-");
        noticePaymentData.setYear(date[0]);
        noticePaymentData.setMonth(date[1]);
        noticePaymentData.setDay(date[2]);
        noticePaymentData.setLocation(location);
        int size = files.size();
        Asserts.check(size==2,"图片必须为2张！");
        MultipartFile image1 = files.get(0);
        MultipartFile image2 = files.get(1);
        InputStream inputStream1 = image1.getInputStream();
        InputStream inputStream2 = image2.getInputStream();
//        PictureRenderData pictureRenderData1 = Pictures.ofStream(inputStream1, PictureType.JPEG).size(520, 290).create();
//        noticePaymentData.setPictureA(pictureRenderData1);
//        PictureRenderData pi9ctureRenderData2 = Pictures.ofStream(inputStream2,PictureType.JPEG).size(520, 290).create();
//        noticePaymentData.setPictureB(pictureRenderData2);
//
////        String resource="template/模板.docx";
//        String resource="/template/模板.docx";
//        String target="/data/bee/file/"+fileName+".docx";
//        PoiTlUtils.noticePayment(noticePaymentData,resource,target);
        try {
            inputStream1 = image1.getInputStream();
            inputStream2 = image2.getInputStream();
            PictureRenderData pictureRenderData1 = Pictures.ofStream(inputStream1, PictureType.JPEG).size(520, 290).create();
            noticePaymentData.setPictureA(pictureRenderData1);
            PictureRenderData pictureRenderData2 = Pictures.ofStream(inputStream2, PictureType.JPEG).size(520, 290).create();
            noticePaymentData.setPictureB(pictureRenderData2);

            // 模板文件路径
            String resource = "/template/模板.docx";
            // 生成文件的路径
            String target = "/data/bee/file/" + fileName + ".docx";
            // 生成文件
            PoiTlUtils.noticePayment(noticePaymentData, resource, target);

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName + ".docx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);

            // 读取生成的文件并写入响应输出流
            try (FileInputStream fis = new FileInputStream(target);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream1 != null) {
                    inputStream1.close();
                }
                if (inputStream2 != null) {
                    inputStream2.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
