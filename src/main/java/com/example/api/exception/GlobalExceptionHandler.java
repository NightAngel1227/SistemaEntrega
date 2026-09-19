package com.example.api.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.api.dto.response.ErroResponseDTO;

/**
 * Tratamento centralizado de exceções da API: converte cada tipo de erro em uma
 * resposta HTTP consistente, sem expor detalhes internos da aplicação.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErroResponseDTO> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErroResponseDTO> handleBusiness(BusinessException ex, WebRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", ex.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErroResponseDTO> handleDuplicate(DuplicateResourceException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, "Recurso duplicado", ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Erro de validação", "Um ou mais campos são inválidos", request, detalhes);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponseDTO> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", "Usuário ou senha incorretos", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponseDTO> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para executar esta ação", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> handleGeneric(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado. Tente novamente mais tarde.", request, null);
    }

    private ResponseEntity<ErroResponseDTO> build(HttpStatus status, String erro, String mensagem,
                                                    WebRequest request, List<String> detalhes) {
        ErroResponseDTO body = ErroResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .erro(erro)
                .mensagem(mensagem)
                .caminho(request.getDescription(false).replace("uri=", ""))
                .detalhes(detalhes)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
