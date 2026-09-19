package com.example.api.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.api.dto.request.ProdutoRequestDTO;
import com.example.api.dto.response.ProdutoResponseDTO;

public interface ProdutoService {
    ProdutoResponseDTO criar(ProdutoRequestDTO dto);
    ProdutoResponseDTO buscarPorId(Long id);
    Page<ProdutoResponseDTO> listar(String nome, Long categoriaId, BigDecimal precoMin, BigDecimal precoMax, Pageable pageable);
    ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto);
    void excluir(Long id);
}
