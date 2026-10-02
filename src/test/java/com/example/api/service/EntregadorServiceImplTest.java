package com.example.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.api.dto.request.EntregadorRequestDTO;
import com.example.api.dto.response.EntregadorResponseDTO;
import com.example.api.exception.DuplicateResourceException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Entregador;
import com.example.api.repository.EntregadorRepository;
import com.example.api.service.impl.EntregadorServiceImpl;

@ExtendWith(MockitoExtension.class)
class EntregadorServiceImplTest {

    @Mock private EntregadorRepository entregadorRepository;

    @InjectMocks
    private EntregadorServiceImpl service;

    private EntregadorRequestDTO dto;
    private Entregador existente;

    @BeforeEach
    void setUp() {
        dto = EntregadorRequestDTO.builder().nome("Carlos").cpf("111.111.111-11")
                .telefone("27999999999").veiculo("Moto").placa("ABC1D23").build();
        existente = Entregador.builder().id(1L).nome("Carlos").cpf("111.111.111-11")
                .telefone("27999999999").veiculo("Moto").placa("ABC1D23").build();
    }

    @Test
    void deveCriarEntregadorDisponivel() {
        when(entregadorRepository.existsByCpf("111.111.111-11")).thenReturn(false);
        when(entregadorRepository.save(any(Entregador.class))).thenAnswer(i -> {
            Entregador e = i.getArgument(0);
            e.setId(1L);
            return e;
        });

        EntregadorResponseDTO resp = service.criar(dto);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getDisponivel()).isTrue();      // todo entregador novo começa disponível
    }

    @Test
    void naoDeveCriarEntregadorComCpfDuplicado() {
        when(entregadorRepository.existsByCpf("111.111.111-11")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(dto))
                .isInstanceOf(DuplicateResourceException.class);
        verify(entregadorRepository, never()).save(any());
    }

    @Test
    void deveBuscarPorId() {
        when(entregadorRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThat(service.buscarPorId(1L).getNome()).isEqualTo("Carlos");
    }

    @Test
    void deveLancarErroAoBuscarEntregadorInexistente() {
        when(entregadorRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveListarTodosQuandoFiltroDisponivelForNulo() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Entregador> page = new PageImpl<>(List.of(existente));
        when(entregadorRepository.findAll(pageable)).thenReturn(page);

        Page<EntregadorResponseDTO> resp = service.listar(null, pageable);

        assertThat(resp.getContent()).hasSize(1);
        verify(entregadorRepository, never()).findByDisponivel(any(), any());
    }

    @Test
    void deveFiltrarPorDisponibilidade() {
        Pageable pageable = PageRequest.of(0, 10);
        when(entregadorRepository.findByDisponivel(true, pageable)).thenReturn(new PageImpl<>(List.of(existente)));

        Page<EntregadorResponseDTO> resp = service.listar(true, pageable);

        assertThat(resp.getContent()).hasSize(1);
        verify(entregadorRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void deveAtualizarMantendoOMesmoCpfSemConsultarDuplicidade() {
        dto.setNome("Carlos Silva");
        when(entregadorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(entregadorRepository.save(any(Entregador.class))).thenAnswer(i -> i.getArgument(0));

        EntregadorResponseDTO resp = service.atualizar(1L, dto);

        assertThat(resp.getNome()).isEqualTo("Carlos Silva");
        verify(entregadorRepository, never()).existsByCpf(any());   // CPF não mudou
    }

    @Test
    void naoDeveAtualizarParaCpfDeOutroEntregador() {
        dto.setCpf("222.222.222-22");
        when(entregadorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(entregadorRepository.existsByCpf("222.222.222-22")).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1L, dto))
                .isInstanceOf(DuplicateResourceException.class);
        verify(entregadorRepository, never()).save(any());
    }

    @Test
    void deveExcluirEntregador() {
        when(entregadorRepository.findById(1L)).thenReturn(Optional.of(existente));

        service.excluir(1L);

        verify(entregadorRepository).delete(existente);
    }

    @Test
    void naoDeveExcluirEntregadorInexistente() {
        when(entregadorRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(9L)).isInstanceOf(ResourceNotFoundException.class);
        verify(entregadorRepository, never()).delete(any());
    }
}