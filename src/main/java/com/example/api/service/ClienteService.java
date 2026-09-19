package com.example.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.api.dto.request.ClienteRequestDTO;
import com.example.api.dto.response.ClienteResponseDTO;

public interface ClienteService {
    ClienteResponseDTO criar(ClienteRequestDTO dto);
    ClienteResponseDTO buscarPorId(Long id);
    Page<ClienteResponseDTO> listar(String nome, Pageable pageable);
    ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dto);
    void excluir(Long id);
}
