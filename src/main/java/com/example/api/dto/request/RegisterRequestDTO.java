package com.example.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.api.model.enums.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Usuário é obrigatório")
    private String username;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres")
    private String password;

    @NotNull(message = "Papel (role) é obrigatório")
    private Role role;
}
