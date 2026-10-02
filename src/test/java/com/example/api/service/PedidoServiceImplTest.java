package com.example.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.dto.request.ItemPedidoRequestDTO;
import com.example.api.dto.request.PedidoRequestDTO;
import com.example.api.dto.response.PedidoResponseDTO;
import com.example.api.exception.BusinessException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Cliente;
import com.example.api.model.Entregador;
import com.example.api.model.ItemPedido;
import com.example.api.model.Pedido;
import com.example.api.model.Produto;
import com.example.api.model.enums.StatusPedido;
import com.example.api.repository.ClienteRepository;
import com.example.api.repository.EntregadorRepository;
import com.example.api.repository.PedidoRepository;
import com.example.api.repository.ProdutoRepository;
import com.example.api.service.impl.PedidoServiceImpl;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplRegrasTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private EntregadorRepository entregadorRepository;
    @Mock private ProdutoRepository produtoRepository;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Cliente cliente;
    private Produto produto;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder().id(1L).nome("João").email("joao@email.com").build();
        produto = Produto.builder().id(1L).nome("Refrigerante")
                .preco(BigDecimal.valueOf(10)).estoque(5).build();
    }

    /** Método auxiliar: cria um pedido já pronto, só para não repetir código nos testes. */
    private Pedido pedidoComStatus(StatusPedido status) {
        return Pedido.builder().id(1L).cliente(cliente).status(status)
                .itens(List.of()).valorTotal(BigDecimal.TEN).enderecoEntrega("Rua X").build();
    }

    // ---------- criar ----------

    @Test
    void deveSomarOTotalDeVariosItens() {
        Produto outro = Produto.builder().id(2L).nome("Pizza")
                .preco(BigDecimal.valueOf(30)).estoque(10).build();
        PedidoRequestDTO dto = PedidoRequestDTO.builder()
                .clienteId(1L).enderecoEntrega("Rua X")
                .itens(List.of(
                        ItemPedidoRequestDTO.builder().produtoId(1L).quantidade(2).build(),   // 2 x 10 = 20
                        ItemPedidoRequestDTO.builder().produtoId(2L).quantidade(1).build()))  // 1 x 30 = 30
                .build();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(outro));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        PedidoResponseDTO resp = pedidoService.criar(dto);

        assertThat(resp.getValorTotal()).isEqualByComparingTo("50");
        assertThat(resp.getItens()).hasSize(2);
        assertThat(produto.getEstoque()).isEqualTo(3);
        assertThat(outro.getEstoque()).isEqualTo(9);
    }

    @Test
    void naoDeveCriarPedidoComProdutoInexistente() {
        PedidoRequestDTO dto = PedidoRequestDTO.builder()
                .clienteId(1L).enderecoEntrega("Rua X")
                .itens(List.of(ItemPedidoRequestDTO.builder().produtoId(99L).quantidade(1).build()))
                .build();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criar(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Produto não encontrado");
        verify(pedidoRepository, never()).save(any());
    }

    // ---------- buscarPorId ----------

    @Test
    void deveLancarErroAoBuscarPedidoInexistente() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------- atualizarStatus ----------

    @ParameterizedTest
    @CsvSource({
            "PENDENTE,CONFIRMADO", "PENDENTE,CANCELADO",
            "CONFIRMADO,EM_ROTA", "CONFIRMADO,CANCELADO",
            "EM_ROTA,ENTREGUE", "EM_ROTA,CANCELADO"
    })
    void devePermitirTransicoesValidas(StatusPedido de, StatusPedido para) {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoComStatus(de)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        PedidoResponseDTO resp = pedidoService.atualizarStatus(1L, para);

        assertThat(resp.getStatus()).isEqualTo(para);
    }

    @ParameterizedTest
    @CsvSource({
            "PENDENTE,EM_ROTA", "PENDENTE,ENTREGUE",
            "CONFIRMADO,ENTREGUE", "CONFIRMADO,PENDENTE",
            "EM_ROTA,PENDENTE",
            "ENTREGUE,CANCELADO", "ENTREGUE,EM_ROTA",
            "CANCELADO,PENDENTE", "CANCELADO,CONFIRMADO"
    })
    void deveRejeitarTransicoesInvalidas(StatusPedido de, StatusPedido para) {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoComStatus(de)));

        assertThatThrownBy(() -> pedidoService.atualizarStatus(1L, para))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida");
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void deveDevolverEstoqueELiberarEntregadorAoCancelar() {
        Entregador entregador = Entregador.builder().id(1L).nome("Carlos").disponivel(false).build();
        ItemPedido item = ItemPedido.builder().produto(produto).quantidade(2)
                .precoUnitario(BigDecimal.TEN).build();
        Pedido pedido = pedidoComStatus(StatusPedido.CONFIRMADO);
        pedido.setEntregador(entregador);
        pedido.setItens(List.of(item));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        PedidoResponseDTO resp = pedidoService.atualizarStatus(1L, StatusPedido.CANCELADO);

        assertThat(resp.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(produto.getEstoque()).isEqualTo(7);          // 5 + 2
        assertThat(entregador.getDisponivel()).isTrue();
        verify(produtoRepository).save(produto);
        verify(entregadorRepository).save(entregador);
    }

    @Test
    void deveLiberarEntregadorAoEntregarSemMexerNoEstoque() {
        Entregador entregador = Entregador.builder().id(1L).nome("Carlos").disponivel(false).build();
        ItemPedido item = ItemPedido.builder().produto(produto).quantidade(2)
                .precoUnitario(BigDecimal.TEN).build();
        Pedido pedido = pedidoComStatus(StatusPedido.EM_ROTA);
        pedido.setEntregador(entregador);
        pedido.setItens(List.of(item));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        pedidoService.atualizarStatus(1L, StatusPedido.ENTREGUE);

        assertThat(entregador.getDisponivel()).isTrue();
        assertThat(produto.getEstoque()).isEqualTo(5);           // não mudou
        verify(produtoRepository, never()).save(any());
    }

    // ---------- atribuirEntregador ----------

    @ParameterizedTest
    @CsvSource({"ENTREGUE", "CANCELADO"})
    void naoDeveAtribuirEntregadorAPedidoFinalizado(StatusPedido status) {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoComStatus(status)));

        assertThatThrownBy(() -> pedidoService.atribuirEntregador(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Não é possível atribuir entregador");
    }

    @Test
    void deveLancarErroQuandoEntregadorNaoExiste() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoComStatus(StatusPedido.PENDENTE)));
        when(entregadorRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.atribuirEntregador(1L, 9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Entregador não encontrado");
    }

    @Test
    void deveLiberarOEntregadorAnteriorAoTrocar() {
        Entregador anterior = Entregador.builder().id(1L).nome("Carlos").disponivel(false).build();
        Entregador novo = Entregador.builder().id(2L).nome("Ana").disponivel(true).build();
        Pedido pedido = pedidoComStatus(StatusPedido.CONFIRMADO);
        pedido.setEntregador(anterior);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(entregadorRepository.findById(2L)).thenReturn(Optional.of(novo));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        PedidoResponseDTO resp = pedidoService.atribuirEntregador(1L, 2L);

        assertThat(anterior.getDisponivel()).isTrue();
        assertThat(novo.getDisponivel()).isFalse();
        assertThat(resp.getEntregadorId()).isEqualTo(2L);
        assertThat(resp.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
    }

    // ---------- excluir ----------

    @Test
    void deveExcluirPedidoPendenteEDevolverEstoque() {
        ItemPedido item = ItemPedido.builder().produto(produto).quantidade(3)
                .precoUnitario(BigDecimal.TEN).build();
        Pedido pedido = pedidoComStatus(StatusPedido.PENDENTE);
        pedido.setItens(List.of(item));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        pedidoService.excluir(1L);

        assertThat(produto.getEstoque()).isEqualTo(8);           // 5 + 3
        verify(pedidoRepository).delete(pedido);
    }

    @Test
    void naoDeveExcluirPedidoQueNaoEstaPendente() {
        Pedido pedido = pedidoComStatus(StatusPedido.CONFIRMADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.excluir(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Somente pedidos com status PENDENTE");
        verify(pedidoRepository, never()).delete(any());
    }
}