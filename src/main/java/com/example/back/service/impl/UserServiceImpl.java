package com.example.back.service.impl;

import cn.hutool.Hutool;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back.mapper.UserMapper;
import com.example.back.model.dto.front2backDTO.LoginFormDTO;
import com.example.back.model.pojo.UserPojo;
import com.example.back.model.vo.UserInfoVO;
import com.example.back.service.UserService;
import com.example.back.util.ResultUtil;
import com.example.back.util.SHAUtil;
import com.example.back.util.TokenUtil;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPojo> implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${file.basicLocation}")
    private String fileLocation;

    @Override
    public ResultUtil login(LoginFormDTO loginFormDTO) {
        QueryWrapper<UserPojo> userQueryWrapper = new QueryWrapper<>();
//        密码SHA512加密
//        String password = SHAUtil.getSHA512(loginFormDTO.getPassword(),false);
        String password = loginFormDTO.getPassword();
        userQueryWrapper.eq("username",loginFormDTO.getUsername()).eq("password",password);
        List<UserPojo> userPojoList = list(userQueryWrapper);
        if(userPojoList.size() == 0) return new ResultUtil(HttpStatus.FORBIDDEN.value(), "账号或者密码错误");
        UserPojo userPojo = userPojoList.get(0);
        String token = TokenUtil.sign(userPojo);
        Map<String,Object> map = new HashMap<>();
        map.put("token",token);
        map.put("type",userPojo.getType());
        return new ResultUtil(HttpStatus.OK.value(), map);
    }

    @Override
    public ResultUtil listUser(Integer pageNum,Integer pageSize) {
        QueryWrapper<UserPojo> userPojoQueryWrapper = new QueryWrapper<>();
        userPojoQueryWrapper.eq("is_delete",0);
        PageHelper.startPage(pageNum,pageSize);
        List<UserPojo> userPojoList = list(userPojoQueryWrapper);
        List<UserInfoVO> userInfoVOList = new ArrayList<>();
        for(UserPojo item : userPojoList){
            userInfoVOList.add(new UserInfoVO(item));
        }
        return ResultUtil.ok(userInfoVOList);
    }

    @Override
    public ResultUtil getUserNumber() {
        QueryWrapper<UserPojo> userPojoQueryWrapper = new QueryWrapper<>();
        userPojoQueryWrapper.eq("is_delete",0);
        return ResultUtil.ok(count(userPojoQueryWrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil createUser(LoginFormDTO loginFormDTO){
        QueryWrapper<UserPojo> userPojoQueryWrapper = new QueryWrapper<>();
        userPojoQueryWrapper.eq("username",loginFormDTO.getUsername());
        if(list(userPojoQueryWrapper).size()!=0) return ResultUtil.fail("已经存在该用户");
        try {
            String uuid = IdUtil.getSnowflakeNextIdStr();
            UserPojo userPojo = new UserPojo(uuid,loginFormDTO.getUsername(),loginFormDTO.getPassword(),1,0);
            save(userPojo);
            String path = fileLocation+'/'+uuid;
            File file = new File(path);
            if(file.mkdir()){

            }
            else{
                throw new Exception("创建文件加失败");
            }
        }
        catch (Exception e){
            logger.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.fail(e.getMessage());
        }
        return ResultUtil.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil changePassword(String uuid,String password) {
        UserPojo userPojo = getById(uuid);
        if(userPojo == null) return ResultUtil.fail("无效用户");
        userPojo.setPassword(password);
        try {
            if(updateById(userPojo)){
                return ResultUtil.ok();
            }
            else{
                return ResultUtil.fail("修改失败");
            }
        }
        catch (Exception e){
            logger.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.fail(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultUtil deleteUser(String uuid) {
        try {
            UserPojo userPojo = new UserPojo();
            userPojo.setUuid(uuid);
            userPojo.setIsDelete(1);
            if(updateById(userPojo)) return ResultUtil.ok();
            else return ResultUtil.fail("删除失败");
        }
        catch (Exception e){
            logger.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.fail(e.getMessage());
        }
    }
}
