package com.example.back.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back.mapper.FileMapper;
import com.example.back.model.dto.CurrentUser;
import com.example.back.model.pojo.FilePojo;
import com.example.back.model.vo.FileInfoVO;
import com.example.back.service.FileService;
import com.example.back.util.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FilePojo> implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Value("${file.basicLocation}")
    private String fileLocation;

    @Override
    public List<FileInfoVO> getChildrenFilesByUuid(String fatherFolder) {
        ObjectMapper objectMapper = new ObjectMapper();
        CurrentUser currentUser = objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
        QueryWrapper<FilePojo> filePojoQueryWrapper = new QueryWrapper<>();
        filePojoQueryWrapper.eq("father_folder",fatherFolder).ne("file_status",0).eq("file_owner",currentUser.getUuid());
        List<FilePojo> filePojoList =list(filePojoQueryWrapper);
        List<FileInfoVO> fileInfoVOList = new ArrayList<>();
        for(FilePojo item : filePojoList){
            fileInfoVOList.add(new FileInfoVO(item));
        }
        return fileInfoVOList;
    }

    @Override
    public Boolean isFileExist(String fatherFolderUuid, String fileName, String fileOwner) {
        QueryWrapper<FilePojo> filePojoQueryWrapper = new QueryWrapper<>();
        filePojoQueryWrapper.eq("father_folder",fatherFolderUuid).eq("file_name",fileName).eq("file_owner",fileOwner);
        List<FilePojo> filePojoList = list(filePojoQueryWrapper);
        if(filePojoList.size()!=0){
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean createFolder(String fatherFolderUuid, String fileName, String fileOwner) {
        String fatherURL = "";
        if(!fatherFolderUuid.isEmpty()){
            fatherURL = getById(fatherFolderUuid).getFileURl();
        }
        try{
            FilePojo filePojo = new FilePojo();
            filePojo.setUuid(IdUtil.getSnowflakeNextIdStr());
            filePojo.setFileOwner(fileOwner);
            filePojo.setFatherFolder(fatherFolderUuid);
            filePojo.setFileName(fileName);
            filePojo.setFileURl(fatherURL+"/"+fileName);
            filePojo.setFileSize(0L);
            filePojo.setIsFolder(1);
            filePojo.setFileUpdateTime(new Date());
            save(filePojo);
            File file = new File(fileLocation+"/"+fileOwner+fatherURL,fileName);
            if(file.mkdir()){

            }
            else{
                throw new Exception("创建文件加失败");
            }
        }
        catch (Exception e){
            logger.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
        return true;
    }

}
