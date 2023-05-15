package com.example.back.model.vo;

import com.example.back.model.pojo.UserPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {

    private String uuid;

    private String username;

    private Integer type;

    public UserInfoVO(UserPojo userPojo) {
        this.uuid=userPojo.getUuid();
        this.username=userPojo.getUsername();
        this.type=userPojo.getType();
    }
}
