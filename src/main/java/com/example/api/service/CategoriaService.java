package com.example.api.service;

import java.util.List;

import com.example.api.dto.request.CategoriaRequestDTO;
import com.example.api.dto.response.CategoriaResponseDTO;

public interface CategoriaService {
    CategoriaResponseDTO criar(CategoriaRequestDTO dto);
    CategoriaResponseDTO buscarPorId(Long id);
    List<CategoriaResponseDTO> listar();
    CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto);
    void excluir(Long id);
}
