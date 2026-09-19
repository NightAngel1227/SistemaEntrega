package com.example.api.mapper;

import com.example.api.dto.request.EnderecoRequestDTO;
import com.example.api.dto.response.EnderecoResponseDTO;
import com.example.api.model.Endereco;

public class EnderecoMapper {

    private EnderecoMapper() {
    }

    public static Endereco toEntity(EnderecoRequestDTO dto) {
        if (dto == null) return null;
        return Endereco.builder()
                .rua(dto.getRua())
                .numero(dto.getNumero())
                .complemento(dto.getComplemento())
                .bairro(dto.getBairro())
                .cidade(dto.getCidade())
                .estado(dto.getEstado())
                .cep(dto.getCep())
                .build();
    }

    public static EnderecoResponseDTO toResponseDTO(Endereco entity) {
        if (entity == null) return null;
        return EnderecoResponseDTO.builder()
                .id(entity.getId())
                .rua(entity.getRua())
                .numero(entity.getNumero())
                .complemento(entity.getComplemento())
                .bairro(entity.getBairro())
                .cidade(entity.getCidade())
                .estado(entity.getEstado())
                .cep(entity.getCep())
                .build();
    }
}
