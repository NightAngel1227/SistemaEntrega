package com.example.api.mapper;

import com.example.api.dto.request.ClienteRequestDTO;
import com.example.api.dto.response.ClienteResponseDTO;
import com.example.api.model.Cliente;

public class ClienteMapper {

    private ClienteMapper() {
    }

    public static Cliente toEntity(ClienteRequestDTO dto) {
        return Cliente.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .telefone(dto.getTelefone())
                .endereco(EnderecoMapper.toEntity(dto.getEndereco()))
                .build();
    }

    public static ClienteResponseDTO toResponseDTO(Cliente entity) {
        return ClienteResponseDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .email(entity.getEmail())
                .telefone(entity.getTelefone())
                .endereco(EnderecoMapper.toResponseDTO(entity.getEndereco()))
                .dataCadastro(entity.getDataCadastro())
                .build();
    }
}
