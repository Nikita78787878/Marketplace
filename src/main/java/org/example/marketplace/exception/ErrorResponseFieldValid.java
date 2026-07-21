package org.example.marketplace.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseFieldValid(LocalDateTime timestamp, int status, Map<String, String> error) {
    public ErrorResponseFieldValid(int status, Map<String, String> error) {
        this(LocalDateTime.now(), status, error);
    }
}
