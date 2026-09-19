package com.example.api.dto.response;

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
public class EntregadorResponseDTO {
    private Long id;
    private String nome;
    private String cpf;
    private String telefone;
    private String veiculo;
    private String placa;
    private Boolean disponivel;
}
