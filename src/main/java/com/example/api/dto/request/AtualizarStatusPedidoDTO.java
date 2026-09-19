package com.example.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.api.model.enums.StatusPedido;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarStatusPedidoDTO {

    @NotNull(message = "Status é obrigatório")
    private StatusPedido status;
}
