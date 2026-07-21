package org.example.marketplace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(NotFoundException.class) // Смотрим где этот класс вызывается перехватывае и выкидываем
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex){
        ErrorResponse body = new ErrorResponse( 404, ex.getMessage()); // наш формат ошибок
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) //внутриняя ошибка
    public ResponseEntity<ErrorResponseFieldValid> handleValidation(MethodArgumentNotValidException ex){

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

        ErrorResponseFieldValid body = ErrorResponseFieldValid.of(400, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
