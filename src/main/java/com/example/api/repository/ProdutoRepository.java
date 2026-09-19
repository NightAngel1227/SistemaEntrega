package com.example.api.repository;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.api.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Consulta personalizada com filtros opcionais (nome, categoria, faixa de preço)
    @Query("""
            SELECT p FROM Produto p
            WHERE (:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
            AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
            AND (:precoMin IS NULL OR p.preco >= :precoMin)
            AND (:precoMax IS NULL OR p.preco <= :precoMax)
            """)
    Page<Produto> buscarComFiltros(@Param("nome") String nome,
                                    @Param("categoriaId") Long categoriaId,
                                    @Param("precoMin") BigDecimal precoMin,
                                    @Param("precoMax") BigDecimal precoMax,
                                    Pageable pageable);
}
