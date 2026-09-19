package com.example.api.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.request.CategoriaRequestDTO;
import com.example.api.dto.response.CategoriaResponseDTO;
import com.example.api.exception.BusinessException;
import com.example.api.exception.DuplicateResourceException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.CategoriaMapper;
import com.example.api.model.Categoria;
import com.example.api.repository.CategoriaRepository;
import com.example.api.service.CategoriaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public CategoriaResponseDTO criar(CategoriaRequestDTO dto) {
        if (categoriaRepository.existsByNomeIgnoreCase(dto.getNome())) {
            throw new DuplicateResourceException("Já existe uma categoria com o nome: " + dto.getNome());
        }
        Categoria categoria = CategoriaMapper.toEntity(dto);
        return CategoriaMapper.toResponseDTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Long id) {
        return CategoriaMapper.toResponseDTO(buscarEntidade(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaMapper::toResponseDTO)
                .toList();
    }

    @Override
    public CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = buscarEntidade(id);
        if (!categoria.getNome().equalsIgnoreCase(dto.getNome()) && categoriaRepository.existsByNomeIgnoreCase(dto.getNome())) {
            throw new DuplicateResourceException("Já existe uma categoria com o nome: " + dto.getNome());
        }
        categoria.setNome(dto.getNome());
        return CategoriaMapper.toResponseDTO(categoriaRepository.save(categoria));
    }

    @Override
    public void excluir(Long id) {
        Categoria categoria = buscarEntidade(id);
        if (!categoria.getProdutos().isEmpty()) {
            throw new BusinessException("Não é possível excluir uma categoria que possui produtos vinculados");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com id: " + id));
    }
}
