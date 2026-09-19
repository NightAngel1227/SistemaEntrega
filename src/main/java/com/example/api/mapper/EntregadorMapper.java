package com.example.api.mapper;

import com.example.api.dto.request.EntregadorRequestDTO;
import com.example.api.dto.response.EntregadorResponseDTO;
import com.example.api.model.Entregador;

public class EntregadorMapper {

    private EntregadorMapper() {
    }

    public static Entregador toEntity(EntregadorRequestDTO dto) {
        return Entregador.builder()
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .telefone(dto.getTelefone())
                .veiculo(dto.getVeiculo())
                .placa(dto.getPlaca())
                .disponivel(true)
                .build();
    }

    public static EntregadorResponseDTO toResponseDTO(Entregador entity) {
        return EntregadorResponseDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .cpf(entity.getCpf())
                .telefone(entity.getTelefone())
                .veiculo(entity.getVeiculo())
                .placa(entity.getPlaca())
                .disponivel(entity.getDisponivel())
                .build();
    }
}
