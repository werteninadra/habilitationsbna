package com.bna.habilitationbna.exception;

public class ProfilNotFoundException extends RuntimeException {
    public ProfilNotFoundException() {
        super();
    }

    public ProfilNotFoundException(String message) {
        super(message);
    }

    public ProfilNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
