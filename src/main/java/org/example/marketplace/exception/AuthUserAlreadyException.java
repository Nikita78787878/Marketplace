package org.example.marketplace.exception;

public class AuthUserAlreadyException extends RuntimeException{

    public AuthUserAlreadyException(String message) {
        super(message);
    }
}
