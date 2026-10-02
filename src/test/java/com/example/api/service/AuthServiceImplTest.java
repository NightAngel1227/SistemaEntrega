package com.example.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.api.dto.request.LoginRequestDTO;
import com.example.api.dto.request.RegisterRequestDTO;
import com.example.api.dto.response.LoginResponseDTO;
import com.example.api.exception.DuplicateResourceException;
import com.example.api.model.Usuario;
import com.example.api.model.enums.Role;
import com.example.api.repository.UsuarioRepository;
import com.example.api.security.JwtService;
import com.example.api.security.UsuarioDetailsServiceImpl;
import com.example.api.service.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UsuarioDetailsServiceImpl usuarioDetailsService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserDetails userDetails(String username) {
        return User.builder().username(username).password("hash").roles("ADMIN").build();
    }

    @Test
    void deveFazerLoginEDevolverToken() {
        UserDetails details = userDetails("admin");
        Usuario usuario = Usuario.builder().id(1L).username("admin").password("hash").role(Role.ADMIN).build();

        when(usuarioDetailsService.loadUserByUsername("admin")).thenReturn(details);
        when(jwtService.gerarToken(details)).thenReturn("token-falso");
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        LoginResponseDTO resp = authService.login(new LoginRequestDTO("admin", "123456"));

        assertThat(resp.getToken()).isEqualTo("token-falso");
        assertThat(resp.getTipo()).isEqualTo("Bearer");
        assertThat(resp.getUsername()).isEqualTo("admin");
        assertThat(resp.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void naoDeveGerarTokenQuandoASenhaEstaErrada() {
        // when(...) só serve para métodos que RETORNAM algo.
        // Para métodos void ou para lançar exceção usamos doThrow / thenThrow.
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("senha errada"));

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("admin", "errada")))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    void deveRegistrarUsuarioComSenhaCriptografada() {
        RegisterRequestDTO dto = new RegisterRequestDTO("maria", "123456", Role.OPERADOR);
        UserDetails details = userDetails("maria");

        when(usuarioRepository.existsByUsername("maria")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("HASH");
        when(usuarioDetailsService.loadUserByUsername("maria")).thenReturn(details);
        when(jwtService.gerarToken(details)).thenReturn("token-maria");

        LoginResponseDTO resp = authService.registrar(dto);

        // ArgumentCaptor "captura" o objeto que o service passou para o save()
        ArgumentCaptor<Usuario> captor = forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario salvo = captor.getValue();

        assertThat(salvo.getPassword()).isEqualTo("HASH");       // nunca a senha pura
        assertThat(salvo.getUsername()).isEqualTo("maria");
        assertThat(salvo.getRole()).isEqualTo(Role.OPERADOR);
        assertThat(resp.getToken()).isEqualTo("token-maria");
        assertThat(resp.getRole()).isEqualTo("OPERADOR");
    }

    @Test
    void naoDeveRegistrarUsernameJaExistente() {
        when(usuarioRepository.existsByUsername("maria")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(new RegisterRequestDTO("maria", "123456", Role.ADMIN)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("maria");
        verify(usuarioRepository, never()).save(any());
    }
}