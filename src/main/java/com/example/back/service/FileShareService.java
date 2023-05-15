package com.example.back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back.model.pojo.FileSharePojo;
import com.example.back.util.ResultUtil;

public interface FileShareService extends IService<FileSharePojo> {

    ResultUtil shareFile(String uuid, Integer time);

    ResultUtil getSharedFile(String shareCode);
}
