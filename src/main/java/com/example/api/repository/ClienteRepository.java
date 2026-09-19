package com.example.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmail(String email);

    Page<Cliente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
