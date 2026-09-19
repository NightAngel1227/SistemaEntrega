package com.example.api.service.impl;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.api.dto.request.ProdutoRequestDTO;
import com.example.api.dto.response.ProdutoResponseDTO;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.mapper.ProdutoMapper;
import com.example.api.model.Categoria;
import com.example.api.model.Produto;
import com.example.api.repository.CategoriaRepository;
import com.example.api.repository.ProdutoRepository;
import com.example.api.service.ProdutoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        Categoria categoria = buscarCategoria(dto.getCategoriaId());
        Produto produto = ProdutoMapper.toEntity(dto, categoria);
        return ProdutoMapper.toResponseDTO(produtoRepository.save(produto));
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Long id) {
        return ProdutoMapper.toResponseDTO(buscarEntidade(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listar(String nome, Long categoriaId, BigDecimal precoMin, BigDecimal precoMax, Pageable pageable) {
        return produtoRepository.buscarComFiltros(nome, categoriaId, precoMin, precoMax, pageable)
                .map(ProdutoMapper::toResponseDTO);
    }

    @Override
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        Produto produto = buscarEntidade(id);
        Categoria categoria = buscarCategoria(dto.getCategoriaId());

        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setEstoque(dto.getEstoque());
        produto.setCategoria(categoria);

        return ProdutoMapper.toResponseDTO(produtoRepository.save(produto));
    }

    @Override
    public void excluir(Long id) {
        Produto produto = buscarEntidade(id);
        produtoRepository.delete(produto);
    }

    private Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com id: " + id));
    }
}
