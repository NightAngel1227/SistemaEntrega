package com.example.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.dto.request.LoginRequestDTO;
import com.example.api.dto.request.RegisterRequestDTO;
import com.example.api.dto.response.LoginResponseDTO;
import com.example.api.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Registro e login de usuários da API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registrar")
    @SecurityRequirements
    @Operation(summary = "Registra um novo usuário e retorna um token JWT")
    public ResponseEntity<LoginResponseDTO> registrar(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(dto));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autentica um usuário existente e retorna um token JWT")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
