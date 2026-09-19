package com.example.api.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.api.dto.request.ProdutoRequestDTO;
import com.example.api.dto.response.ProdutoResponseDTO;
import com.example.api.service.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Cadastro e consulta de produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    @Operation(summary = "Cadastra um novo produto")
    public ResponseEntity<ProdutoResponseDTO> criar(@Valid @RequestBody ProdutoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produto pelo id")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista produtos com paginação, ordenação e filtros (nome, categoria, faixa de preço)")
    public ResponseEntity<Page<ProdutoResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(produtoService.listar(nome, categoriaId, precoMin, precoMax, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um produto")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequestDTO dto) {
        return ResponseEntity.ok(produtoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um produto (restrito a administradores)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
