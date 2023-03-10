package com.example.back.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultUtil {

    private Integer code;
    private Object data;

    public static ResultUtil ok(){
        return new ResultUtil(HttpStatus.OK.value(), "");
    }

    public static ResultUtil ok(Object data){
        return new ResultUtil(HttpStatus.OK.value(), data);
    }

    public static ResultUtil fail(String msg){
        return new ResultUtil(HttpStatus.INTERNAL_SERVER_ERROR.value(),msg);
    }

    public static ResultUtil forbidden(){
        return new ResultUtil(HttpStatus.FORBIDDEN.value(), "");
    }
}
