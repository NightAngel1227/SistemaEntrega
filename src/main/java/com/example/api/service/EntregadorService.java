package com.example.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.api.dto.request.EntregadorRequestDTO;
import com.example.api.dto.response.EntregadorResponseDTO;

public interface EntregadorService {
    EntregadorResponseDTO criar(EntregadorRequestDTO dto);
    EntregadorResponseDTO buscarPorId(Long id);
    Page<EntregadorResponseDTO> listar(Boolean disponivel, Pageable pageable);
    EntregadorResponseDTO atualizar(Long id, EntregadorRequestDTO dto);
    void excluir(Long id);
}
