package com.example.api.exception;

/**
 * Lançada quando um recurso solicitado não é encontrado no banco de dados.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
