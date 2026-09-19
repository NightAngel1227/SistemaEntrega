package com.example.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.api.dto.request.ClienteRequestDTO;
import com.example.api.dto.response.ClienteResponseDTO;
import com.example.api.service.ClienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Cadastro e consulta de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Cadastra um novo cliente")
    public ResponseEntity<ClienteResponseDTO> criar(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um cliente pelo id")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista clientes com paginação, ordenação e filtro opcional por nome")
    public ResponseEntity<Page<ClienteResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(clienteService.listar(nome, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um cliente")
    public ResponseEntity<ClienteResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um cliente (restrito a administradores)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
