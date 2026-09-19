package com.example.api.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.api.dto.request.AtualizarStatusPedidoDTO;
import com.example.api.dto.request.PedidoRequestDTO;
import com.example.api.dto.response.PedidoResponseDTO;
import com.example.api.model.enums.StatusPedido;
import com.example.api.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Criação, consulta e gestão do ciclo de vida dos pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Cria um novo pedido, com baixa automática de estoque")
    public ResponseEntity<PedidoResponseDTO> criar(@Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um pedido pelo id")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista pedidos com paginação, ordenação e filtros (status, cliente, período)")
    public ResponseEntity<Page<PedidoResponseDTO>> listar(
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @PageableDefault(size = 10, sort = "dataCriacao") Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listar(status, clienteId, inicio, fim, pageable));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza o status de um pedido respeitando as transições válidas")
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(@PathVariable Long id, @Valid @RequestBody AtualizarStatusPedidoDTO dto) {
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, dto.getStatus()));
    }

    @PatchMapping("/{id}/entregador/{entregadorId}")
    @Operation(summary = "Atribui um entregador disponível a um pedido")
    public ResponseEntity<PedidoResponseDTO> atribuirEntregador(@PathVariable Long id, @PathVariable Long entregadorId) {
        return ResponseEntity.ok(pedidoService.atribuirEntregador(id, entregadorId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um pedido pendente (restrito a administradores)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pedidoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
