package com.example.api.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.api.model.Usuario;
import com.example.api.model.enums.Role;
import com.example.api.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Garante a existência de um usuário administrador padrão na primeira execução,
 * facilitando o teste da API recém-instalada.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = Usuario.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            usuarioRepository.save(admin);
        }
    }
}
