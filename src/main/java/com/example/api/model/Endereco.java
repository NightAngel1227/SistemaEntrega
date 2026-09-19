package com.example.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Endereço de entrega vinculado a um Cliente (relacionamento 1:1).
 */
@Entity
@Table(name = "enderecos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String rua;

    @Column(nullable = false, length = 10)
    private String numero;

    @Column(length = 80)
    private String complemento;

    @Column(nullable = false, length = 80)
    private String bairro;

    @Column(nullable = false, length = 80)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String estado;

    @Column(nullable = false, length = 9)
    private String cep;
}
