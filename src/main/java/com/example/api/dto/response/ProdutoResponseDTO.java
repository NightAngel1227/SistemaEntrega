package com.example.api.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoResponseDTO {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer estoque;
    private CategoriaResponseDTO categoria;
}
