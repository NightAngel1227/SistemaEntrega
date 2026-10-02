package com.example.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.dto.request.CategoriaRequestDTO;
import com.example.api.dto.response.CategoriaResponseDTO;
import com.example.api.exception.BusinessException;
import com.example.api.exception.DuplicateResourceException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Categoria;
import com.example.api.model.Produto;
import com.example.api.repository.CategoriaRepository;
import com.example.api.service.impl.CategoriaServiceImpl;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl service;

    private Categoria bebidas;

    @BeforeEach
    void setUp() {
        bebidas = Categoria.builder().id(1L).nome("Bebidas").produtos(new ArrayList<>()).build();
    }

    @Test
    void deveCriarCategoria() {
        when(categoriaRepository.existsByNomeIgnoreCase("Bebidas")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> {
            Categoria c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        CategoriaResponseDTO resp = service.criar(new CategoriaRequestDTO("Bebidas"));

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getNome()).isEqualTo("Bebidas");
    }

    @Test
    void naoDeveCriarCategoriaComNomeDuplicado() {
        when(categoriaRepository.existsByNomeIgnoreCase("bebidas")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new CategoriaRequestDTO("bebidas")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void deveListarCategorias() {
        when(categoriaRepository.findAll()).thenReturn(List.of(bebidas));

        assertThat(service.listar()).extracting(CategoriaResponseDTO::getNome).containsExactly("Bebidas");
    }

    @Test
    void deveLancarErroAoBuscarCategoriaInexistente() {
        when(categoriaRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(9L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveAtualizarNomeDaCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(bebidas));
        when(categoriaRepository.existsByNomeIgnoreCase("Sucos")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));

        CategoriaResponseDTO resp = service.atualizar(1L, new CategoriaRequestDTO("Sucos"));

        assertThat(resp.getNome()).isEqualTo("Sucos");
    }

    @Test
    void devePermitirAlterarSoAsLetrasMaiusculasDoMesmoNome() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(bebidas));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));

        CategoriaResponseDTO resp = service.atualizar(1L, new CategoriaRequestDTO("BEBIDAS"));

        assertThat(resp.getNome()).isEqualTo("BEBIDAS");
        verify(categoriaRepository, never()).existsByNomeIgnoreCase(any());
    }

    @Test
    void naoDeveAtualizarParaNomeDeOutraCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(bebidas));
        when(categoriaRepository.existsByNomeIgnoreCase("Lanches")).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, new CategoriaRequestDTO("Lanches")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deveExcluirCategoriaSemProdutos() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(bebidas));

        service.excluir(1L);

        verify(categoriaRepository).delete(bebidas);
    }

    @Test
    void naoDeveExcluirCategoriaComProdutosVinculados() {
        bebidas.getProdutos().add(Produto.builder().id(1L).nome("Suco").preco(BigDecimal.ONE).estoque(1).build());
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(bebidas));

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("produtos vinculados");
        verify(categoriaRepository, never()).delete(any());
    }
}