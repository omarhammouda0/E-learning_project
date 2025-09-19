package com.example.demo.exception.types;

import com.example.demo.exception.base.AppException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends AppException {
    public BadRequestException(String code, String message) {
        super( HttpStatus.BAD_REQUEST, code, message);
    }
}