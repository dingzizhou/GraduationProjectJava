package com.example.back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back.model.dto.front2backDTO.LoginFormDTO;
import com.example.back.model.pojo.UserPojo;
import com.example.back.util.ResultUtil;

public interface UserService extends IService<UserPojo> {

    ResultUtil login(LoginFormDTO loginFormDTO);

    ResultUtil listUser(Integer pageNum,Integer pageSize);

    ResultUtil getUserNumber();

    ResultUtil createUser(LoginFormDTO loginFormDTO);

    ResultUtil changePassword(String uuid,String password);

    ResultUtil deleteUser(String uuid);
}
