package com.mindskip.xzs.controller.admin;

import com.github.pagehelper.PageInfo;
import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.service.WordGenService;
import com.mindskip.xzs.viewmodel.admin.user.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController("TempController")
@RequestMapping(value = "/api/temp")
public class TempController extends BaseApiController {

    private final WordGenService wordGenService;

    public TempController(WordGenService wordGenService) {
        this.wordGenService = wordGenService;
    }


    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public RestResponse pageList( @RequestParam("fileName") String fileName,
                                                            @RequestParam("time") String time,
                                                            @RequestParam("location") String location,
                                                           @RequestParam("files") List<MultipartFile> files) {
        log.info("request={}{}{},", fileName,time,files.size());
        return RestResponse.ok(this.wordGenService.genWordWithJLMeeting(fileName,time,location,files));
    }


}
