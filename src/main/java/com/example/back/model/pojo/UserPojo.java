package com.example.back.model.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPojo {
    @TableId
    private String uuid;
    @TableField("username")
    private String username;
    @TableField("password")
    private String password;
    @TableField("type")
    private Integer type;
    @TableField("is_delete")
    private Integer isDelete;
}
