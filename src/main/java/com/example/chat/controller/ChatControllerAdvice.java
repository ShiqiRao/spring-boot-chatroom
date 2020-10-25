package com.example.chat.controller;

import com.example.chat.model.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class ChatControllerAdvice {

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<R<String>> errorHandler(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("未处理异常" + errorMsg);
        return new ResponseEntity<>(new R<String>()
                .setMessage("参数错误：" + errorMsg), HttpStatus.BAD_REQUEST);
    }

}
