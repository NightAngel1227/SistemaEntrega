package com.example.api.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.request.ClienteRequestDTO;
import com.example.api.dto.response.ClienteResponseDTO;
import com.example.api.exception.DuplicateResourceException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.ClienteMapper;
import com.example.api.mapper.EnderecoMapper;
import com.example.api.model.Cliente;
import com.example.api.repository.ClienteRepository;
import com.example.api.service.ClienteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    public ClienteResponseDTO criar(ClienteRequestDTO dto) {
        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Já existe um cliente cadastrado com o e-mail: " + dto.getEmail());
        }
        Cliente cliente = ClienteMapper.toEntity(dto);
        return ClienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        return ClienteMapper.toResponseDTO(buscarEntidade(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listar(String nome, Pageable pageable) {
        Page<Cliente> page = (nome == null || nome.isBlank())
                ? clienteRepository.findAll(pageable)
                : clienteRepository.findByNomeContainingIgnoreCase(nome, pageable);
        return page.map(ClienteMapper::toResponseDTO);
    }

    @Override
    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = buscarEntidade(id);

        if (!cliente.getEmail().equalsIgnoreCase(dto.getEmail()) && clienteRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Já existe um cliente cadastrado com o e-mail: " + dto.getEmail());
        }

        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());

        if (cliente.getEndereco() == null) {
            cliente.setEndereco(EnderecoMapper.toEntity(dto.getEndereco()));
        } else {
            var novo = EnderecoMapper.toEntity(dto.getEndereco());
            var atual = cliente.getEndereco();
            atual.setRua(novo.getRua());
            atual.setNumero(novo.getNumero());
            atual.setComplemento(novo.getComplemento());
            atual.setBairro(novo.getBairro());
            atual.setCidade(novo.getCidade());
            atual.setEstado(novo.getEstado());
            atual.setCep(novo.getCep());
        }

        return ClienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    @Override
    public void excluir(Long id) {
        Cliente cliente = buscarEntidade(id);
        clienteRepository.delete(cliente);
    }

    private Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com id: " + id));
    }
}
