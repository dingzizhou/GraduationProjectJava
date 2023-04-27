package com.example.back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back.model.pojo.FilePojo;
import com.example.back.model.vo.FileInfoVO;

import java.util.List;

public interface FileService extends IService<FilePojo>  {

    List<FileInfoVO> getChildrenFilesByUuid(String fatherFolder);

    Boolean isFileExist(String fatherFolderUuid,String fileName,String fileOwner);

    Boolean createFolder(String fatherFolderUuid,String fileName,String fileOwner);
}
