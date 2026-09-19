package com.example.api.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.request.ItemPedidoRequestDTO;
import com.example.api.dto.request.PedidoRequestDTO;
import com.example.api.dto.response.PedidoResponseDTO;
import com.example.api.exception.BusinessException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.PedidoMapper;
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
import com.example.api.service.PedidoService;

import lombok.RequiredArgsConstructor;

/**
 * Regras de negócio do fluxo de pedidos:
 * - baixa e reposição automática de estoque conforme o ciclo de vida do pedido;
 * - transições de status controladas por uma máquina de estados simples;
 * - entregador só pode ser atribuído se estiver disponível, e passa a ficar
 *   indisponível enquanto estiver em rota.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final EntregadorRepository entregadorRepository;
    private final ProdutoRepository produtoRepository;

    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES_VALIDAS = new EnumMap<>(StatusPedido.class);
    static {
        TRANSICOES_VALIDAS.put(StatusPedido.PENDENTE, Set.of(StatusPedido.CONFIRMADO, StatusPedido.CANCELADO));
        TRANSICOES_VALIDAS.put(StatusPedido.CONFIRMADO, Set.of(StatusPedido.EM_ROTA, StatusPedido.CANCELADO));
        TRANSICOES_VALIDAS.put(StatusPedido.EM_ROTA, Set.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO));
        TRANSICOES_VALIDAS.put(StatusPedido.ENTREGUE, Set.of());
        TRANSICOES_VALIDAS.put(StatusPedido.CANCELADO, Set.of());
    }

    @Override
    public PedidoResponseDTO criar(PedidoRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com id: " + dto.getClienteId()));

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .enderecoEntrega(dto.getEnderecoEntrega())
                .observacoes(dto.getObservacoes())
                .status(StatusPedido.PENDENTE)
                .itens(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedidoRequestDTO itemDto : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDto.getProdutoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + itemDto.getProdutoId()));

            if (produto.getEstoque() < itemDto.getQuantidade()) {
                throw new BusinessException("Estoque insuficiente para o produto '" + produto.getNome()
                        + "'. Disponível: " + produto.getEstoque());
            }

            // Regra de negócio: baixa de estoque no momento da criação do pedido
            produto.setEstoque(produto.getEstoque() - itemDto.getQuantidade());
            produtoRepository.save(produto);

            ItemPedido item = ItemPedido.builder()
                    .pedido(pedido)
                    .produto(produto)
                    .quantidade(itemDto.getQuantidade())
                    .precoUnitario(produto.getPreco())
                    .build();

            pedido.getItens().add(item);
            total = total.add(item.getSubtotal());
        }

        pedido.setValorTotal(total);

        return PedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(Long id) {
        return PedidoMapper.toResponseDTO(buscarEntidade(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listar(StatusPedido status, Long clienteId, LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        return pedidoRepository.buscarComFiltros(status, clienteId, inicio, fim, pageable)
                .map(PedidoMapper::toResponseDTO);
    }

    @Override
    public PedidoResponseDTO atualizarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = buscarEntidade(id);
        StatusPedido atual = pedido.getStatus();

        if (!TRANSICOES_VALIDAS.getOrDefault(atual, Set.of()).contains(novoStatus)) {
            throw new BusinessException("Transição de status inválida: " + atual + " -> " + novoStatus);
        }

        // Regra de negócio: cancelamento devolve os itens ao estoque
        if (novoStatus == StatusPedido.CANCELADO) {
            for (ItemPedido item : pedido.getItens()) {
                Produto produto = item.getProduto();
                produto.setEstoque(produto.getEstoque() + item.getQuantidade());
                produtoRepository.save(produto);
            }
        }

        // Regra de negócio: entregador fica disponível novamente ao concluir ou cancelar a entrega
        if ((novoStatus == StatusPedido.ENTREGUE || novoStatus == StatusPedido.CANCELADO) && pedido.getEntregador() != null) {
            Entregador entregador = pedido.getEntregador();
            entregador.setDisponivel(true);
            entregadorRepository.save(entregador);
        }

        pedido.setStatus(novoStatus);
        pedido.setDataAtualizacao(LocalDateTime.now());

        return PedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    @Override
    public PedidoResponseDTO atribuirEntregador(Long id, Long entregadorId) {
        Pedido pedido = buscarEntidade(id);

        if (pedido.getStatus() == StatusPedido.ENTREGUE || pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new BusinessException("Não é possível atribuir entregador a um pedido " + pedido.getStatus());
        }

        Entregador entregador = entregadorRepository.findById(entregadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Entregador não encontrado com id: " + entregadorId));

        if (Boolean.FALSE.equals(entregador.getDisponivel())) {
            throw new BusinessException("Entregador '" + entregador.getNome() + "' não está disponível no momento");
        }

        // Libera o entregador anterior, se houver troca
        if (pedido.getEntregador() != null && !pedido.getEntregador().getId().equals(entregador.getId())) {
            Entregador anterior = pedido.getEntregador();
            anterior.setDisponivel(true);
            entregadorRepository.save(anterior);
        }

        entregador.setDisponivel(false);
        entregadorRepository.save(entregador);

        pedido.setEntregador(entregador);
        if (pedido.getStatus() == StatusPedido.PENDENTE) {
            pedido.setStatus(StatusPedido.CONFIRMADO);
        }
        pedido.setDataAtualizacao(LocalDateTime.now());

        return PedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    @Override
    public void excluir(Long id) {
        Pedido pedido = buscarEntidade(id);
        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new BusinessException("Somente pedidos com status PENDENTE podem ser excluídos. Utilize o cancelamento para os demais casos.");
        }
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produto.setEstoque(produto.getEstoque() + item.getQuantidade());
            produtoRepository.save(produto);
        }
        pedidoRepository.delete(pedido);
    }

    private Pedido buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com id: " + id));
    }
}
