package com.example.api.mapper;

import com.example.api.dto.request.ProdutoRequestDTO;
import com.example.api.dto.response.ProdutoResponseDTO;
import com.example.api.model.Categoria;
import com.example.api.model.Produto;

public class ProdutoMapper {

    private ProdutoMapper() {
    }

    public static Produto toEntity(ProdutoRequestDTO dto, Categoria categoria) {
        return Produto.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .preco(dto.getPreco())
                .estoque(dto.getEstoque())
                .categoria(categoria)
                .build();
    }

    public static ProdutoResponseDTO toResponseDTO(Produto entity) {
        return ProdutoResponseDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .descricao(entity.getDescricao())
                .preco(entity.getPreco())
                .estoque(entity.getEstoque())
                .categoria(CategoriaMapper.toResponseDTO(entity.getCategoria()))
                .build();
    }
}
