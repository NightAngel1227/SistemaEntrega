package com.example.api.exception;

/**
 * Lançada quando há tentativa de criar um recurso que viola uma restrição de unicidade
 * (ex.: e-mail, CPF ou nome de usuário já cadastrado).
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
