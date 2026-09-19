package com.example.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.api.model.enums.StatusPedido;

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
public class PedidoResponseDTO {
    private Long id;
    private Long clienteId;
    private String clienteNome;
    private Long entregadorId;
    private String entregadorNome;
    private List<ItemPedidoResponseDTO> itens;
    private StatusPedido status;
    private String enderecoEntrega;
    private BigDecimal valorTotal;
    private String observacoes;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
