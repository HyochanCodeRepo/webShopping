package com.example.webshopping.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 커스텀 예외
 */
@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
