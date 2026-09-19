package com.example.api.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.api.dto.request.LoginRequestDTO;
import com.example.api.dto.request.RegisterRequestDTO;
import com.example.api.dto.response.LoginResponseDTO;
import com.example.api.exception.DuplicateResourceException;
import com.example.api.model.Usuario;
import com.example.api.repository.UsuarioRepository;
import com.example.api.security.JwtService;
import com.example.api.security.UsuarioDetailsServiceImpl;
import com.example.api.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsServiceImpl usuarioDetailsService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(dto.getUsername());
        String token = jwtService.gerarToken(userDetails);
        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername()).orElseThrow();

        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .username(usuario.getUsername())
                .role(usuario.getRole().name())
                .build();
    }

    @Override
    public LoginResponseDTO registrar(RegisterRequestDTO dto) {
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Nome de usuário já está em uso: " + dto.getUsername());
        }

        Usuario usuario = Usuario.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();
        usuarioRepository.save(usuario);

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(usuario.getUsername());
        String token = jwtService.gerarToken(userDetails);

        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .username(usuario.getUsername())
                .role(usuario.getRole().name())
                .build();
    }
}
