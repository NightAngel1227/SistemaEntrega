package com.example.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.dto.request.ItemPedidoRequestDTO;
import com.example.api.dto.request.PedidoRequestDTO;
import com.example.api.dto.response.PedidoResponseDTO;
import com.example.api.exception.BusinessException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Categoria;
import com.example.api.model.Cliente;
import com.example.api.model.Entregador;
import com.example.api.model.Pedido;
import com.example.api.model.Produto;
import com.example.api.model.enums.StatusPedido;
import com.example.api.repository.ClienteRepository;
import com.example.api.repository.EntregadorRepository;
import com.example.api.repository.PedidoRepository;
import com.example.api.repository.ProdutoRepository;
import com.example.api.service.impl.PedidoServiceImpl;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private EntregadorRepository entregadorRepository;
    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Cliente cliente;
    private Produto produto;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder().id(1L).nome("João").email("joao@email.com").telefone("27999999999").build();

        Categoria categoria = Categoria.builder().id(1L).nome("Bebidas").build();
        produto = Produto.builder().id(1L).nome("Refrigerante").preco(BigDecimal.valueOf(10))
                .estoque(5).categoria(categoria).build();
    }

    @Test
    void deveCriarPedidoEBaixarEstoque() {
        PedidoRequestDTO dto = PedidoRequestDTO.builder()
                .clienteId(1L)
                .enderecoEntrega("Rua X, 123")
                .itens(List.of(ItemPedidoRequestDTO.builder().produtoId(1L).quantidade(2).build()))
                .build();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        PedidoResponseDTO response = pedidoService.criar(dto);

        assertThat(response.getValorTotal()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(produto.getEstoque()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo(StatusPedido.PENDENTE);
    }

    @Test
    void naoDeveCriarPedidoComEstoqueInsuficiente() {
        PedidoRequestDTO dto = PedidoRequestDTO.builder()
                .clienteId(1L)
                .enderecoEntrega("Rua X, 123")
                .itens(List.of(ItemPedidoRequestDTO.builder().produtoId(1L).quantidade(100).build()))
                .build();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> pedidoService.criar(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");
    }

    @Test
    void naoDeveCriarPedidoParaClienteInexistente() {
        PedidoRequestDTO dto = PedidoRequestDTO.builder()
                .clienteId(99L)
                .enderecoEntrega("Rua X, 123")
                .itens(List.of(ItemPedidoRequestDTO.builder().produtoId(1L).quantidade(1).build()))
                .build();

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criar(dto)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void naoDevePermitirTransicaoDeStatusInvalida() {
        Pedido pedido = Pedido.builder().id(1L).cliente(cliente).status(StatusPedido.ENTREGUE)
                .itens(List.of()).valorTotal(BigDecimal.TEN).enderecoEntrega("Rua X").build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.atualizarStatus(1L, StatusPedido.EM_ROTA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    void naoDeveAtribuirEntregadorIndisponivel() {
        Pedido pedido = Pedido.builder().id(1L).cliente(cliente).status(StatusPedido.PENDENTE)
                .itens(List.of()).valorTotal(BigDecimal.TEN).enderecoEntrega("Rua X").build();
        Entregador entregador = Entregador.builder().id(1L).nome("Carlos").disponivel(false).build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(entregadorRepository.findById(1L)).thenReturn(Optional.of(entregador));

        assertThatThrownBy(() -> pedidoService.atribuirEntregador(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não está disponível");
    }

    @Test
    void deveAtribuirEntregadorDisponivelEConfirmarPedido() {
        Pedido pedido = Pedido.builder().id(1L).cliente(cliente).status(StatusPedido.PENDENTE)
                .itens(List.of()).valorTotal(BigDecimal.TEN).enderecoEntrega("Rua X").build();
        Entregador entregador = Entregador.builder().id(1L).nome("Carlos").disponivel(true).build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(entregadorRepository.findById(1L)).thenReturn(Optional.of(entregador));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponseDTO response = pedidoService.atribuirEntregador(1L, 1L);

        assertThat(response.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
        assertThat(entregador.getDisponivel()).isFalse();
    }
}
