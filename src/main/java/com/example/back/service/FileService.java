package com.example.back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back.model.dto.front2backDTO.CreateFolderDTO;
import com.example.back.model.dto.front2backDTO.FileInfoDTO;
import com.example.back.model.pojo.FilePojo;
import com.example.back.model.vo.FileInfoVO;
import com.example.back.util.ResultUtil;

import java.util.List;

public interface FileService extends IService<FilePojo>  {

    ResultUtil getChildrenFiles(String path);

    ResultUtil goBack(String path);

    Boolean isFileExist(String fatherFolderUuid,String fileName,String fileOwner);

    Boolean isFileExist(String path);

    ResultUtil createFolder(CreateFolderDTO createFolderDTO);

    ResultUtil mergeFile(FileInfoDTO fileInfoDTO);

    ResultUtil renameFile(String filePath,String newName);

    ResultUtil deleteFile(String path);

    String getFileRelativePath(String uuid);
}
