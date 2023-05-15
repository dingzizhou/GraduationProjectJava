package com.example.back.controller;

import com.example.back.model.dto.front2backDTO.LoginFormDTO;
import com.example.back.model.pojo.UserPojo;
import com.example.back.service.UserService;
import com.example.back.util.ResultUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Resource
    private UserService userService;

    /**
     * 新增用户
     * @param loginFormDTO
     * @return
     */
    @PostMapping("/createUser")
    public ResultUtil createUser(LoginFormDTO loginFormDTO){
        return userService.createUser(loginFormDTO);
    }

    /**
     * 获取用户分页列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/listUser")
    public ResultUtil listUser(Integer pageNum,Integer pageSize){
        return userService.listUser(pageNum,pageSize);
    }

    /**
     * 获取用户数量
     * @return
     */
    @GetMapping("/getUserNumber")
    public ResultUtil getUserNumber(){
        return userService.getUserNumber();
    }

    /**
     * 修改密码
     * @param password
     * @param uuid
     * @return
     */
    @PostMapping("/changePassword")
    public ResultUtil changePassword(String password,String uuid){
        return userService.changePassword(uuid,password);
    }

    /**
     * 删除User
     * @param uuid
     * @return
     */
    @PostMapping("/deleteUser")
    public ResultUtil deleteUser(String uuid){
        return userService.deleteUser(uuid);
    }
}
