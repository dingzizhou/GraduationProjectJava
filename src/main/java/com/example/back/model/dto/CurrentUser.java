package com.example.back.model.dto;

import com.example.back.util.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentUser {

    private String uuid;
    private String username;

    public static CurrentUser getCurrentUser(){
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(ThreadLocalUtil.get("currentUser"),CurrentUser.class);
    }

}
