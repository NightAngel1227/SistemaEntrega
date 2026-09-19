package com.example.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.api.dto.request.ClienteRequestDTO;
import com.example.api.dto.request.EnderecoRequestDTO;
import com.example.api.dto.response.ClienteResponseDTO;
import com.example.api.exception.DuplicateResourceException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.Cliente;
import com.example.api.model.Endereco;
import com.example.api.repository.ClienteRepository;
import com.example.api.service.impl.ClienteServiceImpl;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        EnderecoRequestDTO endereco = EnderecoRequestDTO.builder()
                .rua("Rua das Flores").numero("100").bairro("Centro")
                .cidade("Vitória").estado("ES").cep("29000-000")
                .build();

        requestDTO = ClienteRequestDTO.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .telefone("27999999999")
                .endereco(endereco)
                .build();
    }

    @Test
    void deveCriarClienteComSucesso() {
        when(clienteRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        ClienteResponseDTO response = clienteService.criar(requestDTO);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("João Silva");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void naoDeveCriarClienteComEmailDuplicado() {
        when(clienteRepository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.criar(requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(requestDTO.getEmail());
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveBuscarClientePorId() {
        Endereco endereco = Endereco.builder().id(1L).rua("Rua A").numero("1")
                .bairro("B").cidade("C").estado("ES").cep("29000-000").build();
        Cliente cliente = Cliente.builder().id(1L).nome("Maria").email("maria@email.com")
                .telefone("27988887777").endereco(endereco).build();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteResponseDTO response = clienteService.buscarPorId(1L);

        assertThat(response.getNome()).isEqualTo("Maria");
        assertThat(response.getEndereco().getCidade()).isEqualTo("C");
    }
}
