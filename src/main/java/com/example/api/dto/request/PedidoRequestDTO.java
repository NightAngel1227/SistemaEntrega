package com.example.api.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class PedidoRequestDTO {

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    @NotBlank(message = "Endereço de entrega é obrigatório")
    private String enderecoEntrega;

    private String observacoes;

    @NotEmpty(message = "O pedido deve conter ao menos um item")
    @Valid
    private List<ItemPedidoRequestDTO> itens;
}
