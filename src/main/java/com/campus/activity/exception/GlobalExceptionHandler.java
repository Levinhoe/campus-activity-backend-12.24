package com.campus.activity.exception;

import com.campus.activity.common.ApiResult;
import com.campus.activity.common.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().isEmpty()
                ? ErrorCode.BAD_REQUEST.getMsg()
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ApiResult.fail(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Void> handleConstraint(ConstraintViolationException e) {
        return ApiResult.fail(ErrorCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    @ExceptionHandler(BizException.class)
    public ApiResult<Void> handleBiz(BizException e) {
        return ApiResult.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResult<Void> handleDuplicate(DataIntegrityViolationException e) {
        String msg = e.getMostSpecificCause() == null ? "" : e.getMostSpecificCause().getMessage();
        String lower = msg == null ? "" : msg.toLowerCase();
        if (lower.contains("account")) {
            return ApiResult.fail(409, "account already exists");
        }
        if (lower.contains("student_no") || lower.contains("studentno")) {
            return ApiResult.fail(409, "studentNo already exists");
        }
        return ApiResult.fail(409, "data already exists");
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handle(Exception e) {
        return ApiResult.fail(ErrorCode.SYSTEM_ERROR.getCode(), e.getMessage());
    }
}
