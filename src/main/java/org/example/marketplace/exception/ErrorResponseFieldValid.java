package org.example.marketplace.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseFieldValid(LocalDateTime timestamp, int status, Map<String, String> error) {
}
