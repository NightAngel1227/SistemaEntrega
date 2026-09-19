package com.example.api.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.api.dto.request.PedidoRequestDTO;
import com.example.api.dto.response.PedidoResponseDTO;
import com.example.api.model.enums.StatusPedido;

public interface PedidoService {
    PedidoResponseDTO criar(PedidoRequestDTO dto);
    PedidoResponseDTO buscarPorId(Long id);
    Page<PedidoResponseDTO> listar(StatusPedido status, Long clienteId, LocalDateTime inicio, LocalDateTime fim, Pageable pageable);
    PedidoResponseDTO atualizarStatus(Long id, StatusPedido novoStatus);
    PedidoResponseDTO atribuirEntregador(Long id, Long entregadorId);
    void excluir(Long id);
}
