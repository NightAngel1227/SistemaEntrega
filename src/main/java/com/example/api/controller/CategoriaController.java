package com.example.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.api.dto.request.CategoriaRequestDTO;
import com.example.api.dto.response.CategoriaResponseDTO;
import com.example.api.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Cadastro e consulta de categorias de produto")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    @Operation(summary = "Cadastra uma nova categoria")
    public ResponseEntity<CategoriaResponseDTO> criar(@Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma categoria pelo id")
    public ResponseEntity<CategoriaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista todas as categorias")
    public ResponseEntity<List<CategoriaResponseDTO>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma categoria")
    public ResponseEntity<CategoriaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.ok(categoriaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove uma categoria (restrito a administradores)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
