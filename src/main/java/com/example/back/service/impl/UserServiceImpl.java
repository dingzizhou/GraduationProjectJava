package com.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back.mapper.UserMapper;
import com.example.back.model.dto.front2backDTO.LoginFormDTO;
import com.example.back.model.pojo.UserPojo;
import com.example.back.service.UserService;
import com.example.back.util.ResultUtil;
import com.example.back.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPojo> implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public ResultUtil login(LoginFormDTO loginFormDTO) {
        QueryWrapper<UserPojo> userQueryWrapper = new QueryWrapper<>();
//        密码SHA512加密
//        String password = SHAUtil.getSHA512(loginFormDTO.getPassword(),false);
        userQueryWrapper.eq("username",loginFormDTO.getUsername()).eq("password",loginFormDTO.getPassword());
        List<UserPojo> userPojoList = list(userQueryWrapper);
        if(userPojoList.size() == 0) return new ResultUtil(HttpStatus.FORBIDDEN.value(), "账号或者密码错误");
        UserPojo userPojo = userPojoList.get(0);
        String token = TokenUtil.sign(userPojo);
        return new ResultUtil(HttpStatus.OK.value(), token);
    }

}
