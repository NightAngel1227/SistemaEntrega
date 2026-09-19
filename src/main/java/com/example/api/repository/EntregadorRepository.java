package com.example.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.model.Entregador;

public interface EntregadorRepository extends JpaRepository<Entregador, Long> {

    boolean existsByCpf(String cpf);

    Page<Entregador> findByDisponivel(Boolean disponivel, Pageable pageable);
}
