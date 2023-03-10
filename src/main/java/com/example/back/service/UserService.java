package com.example.back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back.model.dto.front2backDTO.LoginFormDTO;
import com.example.back.model.pojo.User;
import com.example.back.util.ResultUtil;

public interface UserService extends IService<User> {

    ResultUtil login(LoginFormDTO loginFormDTO);

}
