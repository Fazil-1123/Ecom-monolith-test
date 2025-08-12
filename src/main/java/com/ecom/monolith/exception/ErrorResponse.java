package com.ecom.monolith.exception;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
    private final String errorCode;

    public ErrorResponse(int status, String error, String message, String errorCode) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.errorCode = errorCode;
    }


}
