package com.example.back.controller;

import com.example.back.model.dto.CurrentUser;
import com.example.back.model.dto.front2backDTO.CreateFolderDTO;
import com.example.back.model.pojo.FilePojo;
import com.example.back.model.vo.FileInfoVO;
import com.example.back.service.FileService;
import com.example.back.util.ResultUtil;
import com.example.back.util.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Resource
    private FileService fileService;

    /**
     * 根据父文件夹查找子文件
     * @param folder 父级目录的uuid
     * @return
     */
    @GetMapping("/getChildrenFilesByFolder")
    public ResultUtil getChildrenFilesByUuid(@RequestParam("folder") String folder){
        List<FileInfoVO> files = fileService.getChildrenFilesByUuid(folder);
        return ResultUtil.ok(files);
    }

    /**
     * 根据当前目录获取上一级目录
     * @param currentFolder 当前目录的uuid
     * @return
     */
    @GetMapping("/goBackByCurrentFolder")
    public ResultUtil goBackByCurrentFolder(@RequestParam("currentFolder") String currentFolder){
        FilePojo filePojo = fileService.getById(currentFolder);
        if(filePojo == null){
            return ResultUtil.notFound();
        }
        Map<String,Object> map = new HashMap<>();
        List<FileInfoVO> fileInfoVOList = fileService.getChildrenFilesByUuid(filePojo.getFatherFolder());
        map.put("fileList",fileInfoVOList);
        map.put("currentFolder",filePojo.getFatherFolder());
        return ResultUtil.ok(map);
    }

    @PostMapping("/createFolder")
    public ResultUtil createFolder(@Valid CreateFolderDTO createFolderDTO){
        ObjectMapper objectMapper = new ObjectMapper();
        CurrentUser currentUser = objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
        if(fileService.isFileExist(createFolderDTO.getCurrentFolder(),createFolderDTO.getFileName(),currentUser.getUuid())){
            return ResultUtil.fail("存在同一名字的文件夹");
        }
        if(fileService.createFolder(createFolderDTO.getCurrentFolder(),createFolderDTO.getFileName(),currentUser.getUuid())){
            return ResultUtil.ok();
        }
        else{
            return ResultUtil.fail("服务器内部问题");
        }
    }



    @GetMapping("/getFileMD5")
    public String getFileMD5(String url) throws IOException {
        return DigestUtils.md5DigestAsHex(new FileInputStream(url));
    }

    @PostMapping("/uploadFile")
    public ResultUtil uploadFile(){
        return null;
    }
}
