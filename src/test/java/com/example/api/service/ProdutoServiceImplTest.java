package com.example.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.dto.request.ProdutoRequestDTO;
import com.example.api.dto.response.ProdutoResponseDTO;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Categoria;
import com.example.api.model.Produto;
import com.example.api.repository.CategoriaRepository;
import com.example.api.repository.ProdutoRepository;
import com.example.api.service.impl.ProdutoServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceImplTest {

    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProdutoServiceImpl produtoService;

    private Categoria categoria;
    private ProdutoRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1L).nome("Bebidas").build();
        requestDTO = ProdutoRequestDTO.builder()
                .nome("Suco de Laranja")
                .descricao("Suco natural 500ml")
                .preco(BigDecimal.valueOf(7.5))
                .estoque(50)
                .categoriaId(1L)
                .build();
    }

    @Test
    void deveCriarProdutoComSucesso() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProdutoResponseDTO response = produtoService.criar(requestDTO);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCategoria().getNome()).isEqualTo("Bebidas");
    }

    @Test
    void naoDeveCriarProdutoComCategoriaInexistente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.criar(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoBuscarProdutoInexistente() {
        when(produtoRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
