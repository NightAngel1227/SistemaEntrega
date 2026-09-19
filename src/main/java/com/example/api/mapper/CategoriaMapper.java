package com.example.api.mapper;

import com.example.api.dto.request.CategoriaRequestDTO;
import com.example.api.dto.response.CategoriaResponseDTO;
import com.example.api.model.Categoria;

public class CategoriaMapper {

    private CategoriaMapper() {
    }

    public static Categoria toEntity(CategoriaRequestDTO dto) {
        return Categoria.builder()
                .nome(dto.getNome())
                .build();
    }

    public static CategoriaResponseDTO toResponseDTO(Categoria entity) {
        return CategoriaResponseDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .build();
    }
}
