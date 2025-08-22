package com.bna.habilitationbna.exception;

/**
 * Exception levée lorsqu'une ressource n'est pas trouvée dans l'application.
 */
public class RessourceNotFoundException extends RuntimeException {

    // Constructeur par défaut
    public RessourceNotFoundException() {
        super("Ressource non trouvée");
    }

    // Constructeur avec message personnalisé
    public RessourceNotFoundException(String message) {
        super(message);
    }

    // Constructeur avec message personnalisé et cause
    public RessourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
