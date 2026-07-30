package org.example.marketplace.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * extends ResponseEntityExceptionHandler — в этом классе Spring уже разложил по правильным кодам свои MVC-исключения (битый JSON → 400,
 * неизвестный URL → 404, неподдержанный метод → 405). Его хендлеры объявлены на конкретные типы, а конкретный тип всегда специфичнее Exception,
 * поэтому Spring выберет их, а не твой. Без наследования всё это валилось бы в твой Exception и отдавало 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex){
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }


    @ExceptionHandler(NotFoundException.class) // Смотрим где этот класс вызывается перехватывае и выкидываем
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex){
        ErrorResponse body = new ErrorResponse( 404, ex.getMessage()); // наш формат ошибок
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

        ErrorResponseFieldValid body = ErrorResponseFieldValid.of(HttpStatus.BAD_REQUEST.value(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
