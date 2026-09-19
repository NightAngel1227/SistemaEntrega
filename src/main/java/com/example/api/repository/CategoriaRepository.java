package com.example.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.api.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNomeIgnoreCase(String nome);
}
