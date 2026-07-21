package org.example.marketplace.exception;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime timestamp, int status, String error) {
    public ErrorResponse(int status, String error) {
        this(LocalDateTime.now(), status, error); // вшил с помощью конструктора
    }
}
