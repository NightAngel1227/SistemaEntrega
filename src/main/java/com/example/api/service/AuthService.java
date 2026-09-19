package com.example.api.service;

import com.example.api.dto.request.LoginRequestDTO;
import com.example.api.dto.request.RegisterRequestDTO;
import com.example.api.dto.response.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO dto);
    LoginResponseDTO registrar(RegisterRequestDTO dto);
}
