package com.example.back.interceptor;

import com.example.back.util.ResultUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一处理方法抛出的异常
 */
@RestControllerAdvice
public class RequestExceptionHandler {

    @ExceptionHandler(value = {BindException.class, ValidationException.class, MethodArgumentNotValidException.class})
    public ResultUtil handleValidatedException(Exception e) {
        System.out.println(e.getMessage());
        StringBuilder msg = new StringBuilder();
        if(e instanceof MethodArgumentNotValidException ex){
            ex.getAllErrors().forEach(objectError -> msg.append(objectError.getDefaultMessage()).append(';'));
        }
        else if(e instanceof BindException ex){
            ex.getAllErrors().forEach(objectError -> msg.append(objectError.getDefaultMessage()).append(';'));
        }
        else if (e instanceof ConstraintViolationException ex) {
            for(ConstraintViolation<?> constraintViolation : ex.getConstraintViolations()){
                msg.append(constraintViolation.getMessage()).append(";");
            }
        }

        return new ResultUtil(HttpStatus.UNPROCESSABLE_ENTITY.value(), msg);
    }

}
