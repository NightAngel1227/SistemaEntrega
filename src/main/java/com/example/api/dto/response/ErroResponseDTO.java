package com.example.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Formato padronizado de resposta de erro (tratamento centralizado de exceções).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErroResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String erro;
    private String mensagem;
    private String caminho;
    private List<String> detalhes;
}
