package com.example.api.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "entregadores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entregador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(nullable = false, length = 40)
    private String veiculo;

    @Column(nullable = false, length = 10)
    private String placa;

    @Builder.Default
    @Column(nullable = false)
    private Boolean disponivel = true;

    // Relacionamento 1:N - um entregador atende vários pedidos ao longo do tempo
    @Builder.Default
    @OneToMany(mappedBy = "entregador", cascade = CascadeType.PERSIST)
    private List<Pedido> pedidos = new ArrayList<>();
}
