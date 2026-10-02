package com.example.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.api.dto.response.PedidoResponseDTO;
import com.example.api.model.Cliente;
import com.example.api.model.Entregador;
import com.example.api.model.ItemPedido;
import com.example.api.model.Pedido;
import com.example.api.model.Produto;
import com.example.api.model.enums.StatusPedido;

/** O mapper é uma classe estática e sem dependências: não precisa de mock nenhum. */
class PedidoMapperTest {

    private final Cliente cliente = Cliente.builder().id(1L).nome("João").build();
    private final Produto produto = Produto.builder().id(7L).nome("Pizza").preco(BigDecimal.valueOf(30)).build();

    @Test
    void deveConverterPedidoSemEntregador() {
        ItemPedido item = ItemPedido.builder().id(5L).produto(produto).quantidade(2)
                .precoUnitario(BigDecimal.valueOf(30)).build();
        Pedido pedido = Pedido.builder().id(10L).cliente(cliente).status(StatusPedido.PENDENTE)
                .itens(List.of(item)).valorTotal(BigDecimal.valueOf(60)).enderecoEntrega("Rua X").build();

        PedidoResponseDTO dto = PedidoMapper.toResponseDTO(pedido);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getClienteId()).isEqualTo(1L);
        assertThat(dto.getClienteNome()).isEqualTo("João");
        assertThat(dto.getEntregadorId()).isNull();       // sem entregador => null, sem erro
        assertThat(dto.getEntregadorNome()).isNull();
        assertThat(dto.getStatus()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(dto.getItens()).hasSize(1);
        assertThat(dto.getItens().get(0).getProdutoNome()).isEqualTo("Pizza");
        assertThat(dto.getItens().get(0).getSubtotal()).isEqualByComparingTo("60");
    }

    @Test
    void deveConverterPedidoComEntregador() {
        Entregador entregador = Entregador.builder().id(3L).nome("Carlos").build();
        Pedido pedido = Pedido.builder().id(10L).cliente(cliente).entregador(entregador)
                .status(StatusPedido.EM_ROTA).itens(List.of())
                .valorTotal(BigDecimal.ZERO).enderecoEntrega("Rua X").build();

        PedidoResponseDTO dto = PedidoMapper.toResponseDTO(pedido);

        assertThat(dto.getEntregadorId()).isEqualTo(3L);
        assertThat(dto.getEntregadorNome()).isEqualTo("Carlos");
        assertThat(dto.getItens()).isEmpty();
    }
}