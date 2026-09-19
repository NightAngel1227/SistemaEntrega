package com.example.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.api.dto.request.EntregadorRequestDTO;
import com.example.api.dto.response.EntregadorResponseDTO;
import com.example.api.service.EntregadorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/entregadores")
@RequiredArgsConstructor
@Tag(name = "Entregadores", description = "Cadastro e consulta de entregadores")
public class EntregadorController {

    private final EntregadorService entregadorService;

    @PostMapping
    @Operation(summary = "Cadastra um novo entregador")
    public ResponseEntity<EntregadorResponseDTO> criar(@Valid @RequestBody EntregadorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(entregadorService.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um entregador pelo id")
    public ResponseEntity<EntregadorResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(entregadorService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista entregadores com paginação, ordenação e filtro por disponibilidade")
    public ResponseEntity<Page<EntregadorResponseDTO>> listar(
            @RequestParam(required = false) Boolean disponivel,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(entregadorService.listar(disponivel, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um entregador")
    public ResponseEntity<EntregadorResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody EntregadorRequestDTO dto) {
        return ResponseEntity.ok(entregadorService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um entregador (restrito a administradores)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        entregadorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
