package com.example.api.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.request.EntregadorRequestDTO;
import com.example.api.dto.response.EntregadorResponseDTO;
import com.example.api.exception.DuplicateResourceException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.EntregadorMapper;
import com.example.api.model.Entregador;
import com.example.api.repository.EntregadorRepository;
import com.example.api.service.EntregadorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EntregadorServiceImpl implements EntregadorService {

    private final EntregadorRepository entregadorRepository;

    @Override
    public EntregadorResponseDTO criar(EntregadorRequestDTO dto) {
        if (entregadorRepository.existsByCpf(dto.getCpf())) {
            throw new DuplicateResourceException("Já existe um entregador cadastrado com o CPF: " + dto.getCpf());
        }
        Entregador entregador = EntregadorMapper.toEntity(dto);
        return EntregadorMapper.toResponseDTO(entregadorRepository.save(entregador));
    }

    @Override
    @Transactional(readOnly = true)
    public EntregadorResponseDTO buscarPorId(Long id) {
        return EntregadorMapper.toResponseDTO(buscarEntidade(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EntregadorResponseDTO> listar(Boolean disponivel, Pageable pageable) {
        Page<Entregador> page = (disponivel == null)
                ? entregadorRepository.findAll(pageable)
                : entregadorRepository.findByDisponivel(disponivel, pageable);
        return page.map(EntregadorMapper::toResponseDTO);
    }

    @Override
    public EntregadorResponseDTO atualizar(Long id, EntregadorRequestDTO dto) {
        Entregador entregador = buscarEntidade(id);

        if (!entregador.getCpf().equals(dto.getCpf()) && entregadorRepository.existsByCpf(dto.getCpf())) {
            throw new DuplicateResourceException("Já existe um entregador cadastrado com o CPF: " + dto.getCpf());
        }

        entregador.setNome(dto.getNome());
        entregador.setCpf(dto.getCpf());
        entregador.setTelefone(dto.getTelefone());
        entregador.setVeiculo(dto.getVeiculo());
        entregador.setPlaca(dto.getPlaca());

        return EntregadorMapper.toResponseDTO(entregadorRepository.save(entregador));
    }

    @Override
    public void excluir(Long id) {
        Entregador entregador = buscarEntidade(id);
        entregadorRepository.delete(entregador);
    }

    private Entregador buscarEntidade(Long id) {
        return entregadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entregador não encontrado com id: " + id));
    }
}
