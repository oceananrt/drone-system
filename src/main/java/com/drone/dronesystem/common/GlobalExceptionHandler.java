package com.drone.dronesystem.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 全局异常处理器
 * 统一捕获并返回标准 Result 格式，避免堆栈泄露
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = Logger.getLogger(GlobalExceptionHandler.class.getName());

    /**
     * 业务异常
     */
    @ExceptionHandler(DroneException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleDroneException(DroneException e) {
        log.log(Level.WARNING, "业务异常: {0}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    /**
     * 参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleIllegalArgument(IllegalArgumentException e) {
        log.log(Level.WARNING, "参数异常: {0}", e.getMessage());
        return Result.fail("参数错误: " + e.getMessage());
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.log(Level.SEVERE, "系统异常", e);
        return Result.fail("系统内部错误，请稍后重试");
    }
}
