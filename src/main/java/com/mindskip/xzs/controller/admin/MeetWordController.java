package com.mindskip.xzs.controller.admin;

import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.service.WordGenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@RestController("MeetWordController")
@RequestMapping(value = "/api/meet/word")
public class MeetWordController extends BaseApiController {

    private final WordGenService wordGenService;

    public MeetWordController(WordGenService wordGenService) {
        this.wordGenService = wordGenService;
    }


    @RequestMapping(value = "/export", method = RequestMethod.POST)
    public void pageList( @RequestParam("fileName") String fileName,
                                                            @RequestParam("time") String time,
                                                            @RequestParam("location") String location,
                                                           @RequestParam("files") List<MultipartFile> files, HttpServletResponse response) {
        log.info("request={}{}{},", fileName,time,files.size());
        this.wordGenService.genWordWithJLMeeting(fileName,time,location,files,response);
    }


}
