package com.example.api.exception;

/**
 * Lançada quando uma regra de negócio é violada (ex.: estoque insuficiente,
 * transição de status inválida, entregador indisponível).
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
