package com.example.api.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.api.model.Pedido;
import com.example.api.model.enums.StatusPedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Consulta personalizada com filtros opcionais (status, cliente, período)
    @Query("""
            SELECT p FROM Pedido p
            WHERE (:status IS NULL OR p.status = :status)
            AND (:clienteId IS NULL OR p.cliente.id = :clienteId)
            AND (:inicio IS NULL OR p.dataCriacao >= :inicio)
            AND (:fim IS NULL OR p.dataCriacao <= :fim)
            """)
    Page<Pedido> buscarComFiltros(@Param("status") StatusPedido status,
                                   @Param("clienteId") Long clienteId,
                                   @Param("inicio") LocalDateTime inicio,
                                   @Param("fim") LocalDateTime fim,
                                   Pageable pageable);
}
