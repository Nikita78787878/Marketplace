package org.example.marketplace.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseFieldValid(LocalDateTime timestamp, int status, Map<String, String> error) {
    //статическая фабрика.
    public static ErrorResponseFieldValid of(int status, Map<String, String> error){
        return new ErrorResponseFieldValid(LocalDateTime.now(), status, error);
    }
}
