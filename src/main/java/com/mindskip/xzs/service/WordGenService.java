package com.mindskip.xzs.service;


import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WordGenService {


    /**
     * 生成接龙卫生院会议记录文档
     *
     * @param fileName
     * @param time
     * @param location
     * @param files
     * @return
     */
    Object  genWordWithJLMeeting(String fileName, String time, String location, List<MultipartFile> files);
}
