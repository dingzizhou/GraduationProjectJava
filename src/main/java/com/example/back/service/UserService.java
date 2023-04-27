package com.example.back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back.model.dto.front2backDTO.LoginFormDTO;
import com.example.back.model.pojo.UserPojo;
import com.example.back.util.ResultUtil;

public interface UserService extends IService<UserPojo> {

    ResultUtil login(LoginFormDTO loginFormDTO);

}
