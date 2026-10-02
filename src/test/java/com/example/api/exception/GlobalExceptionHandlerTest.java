package com.example.api.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import com.example.api.dto.response.ErroResponseDTO;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/teste");
    }

    @Test
    void recursoNaoEncontradoDeveRetornar404() {
        ResponseEntity<ErroResponseDTO> resp = handler.handleNotFound(
                new ResourceNotFoundException("Pedido não encontrado"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().getStatus()).isEqualTo(404);
        assertThat(resp.getBody().getMensagem()).isEqualTo("Pedido não encontrado");
        assertThat(resp.getBody().getCaminho()).isEqualTo("/api/teste");   // "uri=" foi removido
    }

    @Test
    void regraDeNegocioDeveRetornar422() {
        ResponseEntity<ErroResponseDTO> resp = handler.handleBusiness(
                new BusinessException("Estoque insuficiente"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resp.getBody().getMensagem()).isEqualTo("Estoque insuficiente");
    }

    @Test
    void recursoDuplicadoDeveRetornar409() {
        ResponseEntity<ErroResponseDTO> resp = handler.handleDuplicate(
                new DuplicateResourceException("CPF já existe"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void credenciaisInvalidasDevemRetornar401SemVazarDetalhes() {
        ResponseEntity<ErroResponseDTO> resp = handler.handleBadCredentials(
                new BadCredentialsException("detalhe interno"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody().getMensagem()).doesNotContain("detalhe interno");
    }

    @Test
    void acessoNegadoDeveRetornar403() {
        ResponseEntity<ErroResponseDTO> resp = handler.handleAccessDenied(
                new AccessDeniedException("negado"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void erroInesperadoDeveRetornar500SemExporAMensagemOriginal() {
        ResponseEntity<ErroResponseDTO> resp = handler.handleGeneric(
                new RuntimeException("senha do banco é 1234"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody().getMensagem()).doesNotContain("1234");
    }

    @Test
    void erroDeValidacaoDeveRetornar400ComListaDeDetalhes() {
        // MethodArgumentNotValidException é difícil de montar na mão, então usamos mocks
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("dto", "nome", "Nome é obrigatório"),
                new FieldError("dto", "cpf", "CPF é obrigatório")));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErroResponseDTO> resp = handler.handleValidation(ex, request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().getDetalhes())
                .containsExactly("Nome é obrigatório", "CPF é obrigatório");
    }
}