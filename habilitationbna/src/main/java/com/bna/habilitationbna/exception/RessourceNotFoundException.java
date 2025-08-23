package com.bna.habilitationbna.exception;

public class RessourceNotFoundException extends RuntimeException {
    public RessourceNotFoundException() {
        super();
    }

    public RessourceNotFoundException(String message) {
        super(message);
    }

    public RessourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
