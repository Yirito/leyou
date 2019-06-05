package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 用来拦截controller
 * 通用异常处理
 *
 * @ControllerAdvice 异常集中处理，更好的使业务逻辑与异常处理剥离开
 */
@ControllerAdvice
public class CommonExceptionHandler {

    /**
     * 处理LyException返回
     *
     * @ExceptionHandler 统一处理某一类异常
     */
    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handleException(LyException e) {
        return ResponseEntity.status(e.getExceptionEnum().getCode()).body(new ExceptionResult(e.getExceptionEnum()));
    }
}
