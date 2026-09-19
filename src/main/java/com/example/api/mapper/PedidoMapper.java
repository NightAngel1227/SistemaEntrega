package com.example.api.mapper;

import java.util.List;

import com.example.api.dto.response.ItemPedidoResponseDTO;
import com.example.api.dto.response.PedidoResponseDTO;
import com.example.api.model.ItemPedido;
import com.example.api.model.Pedido;

public class PedidoMapper {

    private PedidoMapper() {
    }

    public static ItemPedidoResponseDTO toResponseDTO(ItemPedido item) {
        return ItemPedidoResponseDTO.builder()
                .id(item.getId())
                .produtoId(item.getProduto().getId())
                .produtoNome(item.getProduto().getNome())
                .quantidade(item.getQuantidade())
                .precoUnitario(item.getPrecoUnitario())
                .subtotal(item.getSubtotal())
                .build();
    }

    public static PedidoResponseDTO toResponseDTO(Pedido entity) {
        List<ItemPedidoResponseDTO> itens = entity.getItens().stream()
                .map(PedidoMapper::toResponseDTO)
                .toList();

        return PedidoResponseDTO.builder()
                .id(entity.getId())
                .clienteId(entity.getCliente().getId())
                .clienteNome(entity.getCliente().getNome())
                .entregadorId(entity.getEntregador() != null ? entity.getEntregador().getId() : null)
                .entregadorNome(entity.getEntregador() != null ? entity.getEntregador().getNome() : null)
                .itens(itens)
                .status(entity.getStatus())
                .enderecoEntrega(entity.getEnderecoEntrega())
                .valorTotal(entity.getValorTotal())
                .observacoes(entity.getObservacoes())
                .dataCriacao(entity.getDataCriacao())
                .dataAtualizacao(entity.getDataAtualizacao())
                .build();
    }
}
