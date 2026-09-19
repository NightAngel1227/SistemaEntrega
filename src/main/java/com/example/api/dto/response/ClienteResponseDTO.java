package com.example.api.dto.response;

import java.time.LocalDateTime;

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
public class ClienteResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private EnderecoResponseDTO endereco;
    private LocalDateTime dataCadastro;
}
