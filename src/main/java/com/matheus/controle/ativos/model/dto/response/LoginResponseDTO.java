package com.matheus.controle.ativos.model.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {

    private boolean success;
    private String message;
    private UsuarioResponseDTO user;

    public static LoginResponseDTO sucesso(String message, UUID id, String username, String nome, String role) {
        return new LoginResponseDTO(true, message, new UsuarioResponseDTO(id, username, nome, role));
    }

    public static LoginResponseDTO falha(String message) {
        return new LoginResponseDTO(false, message, null);
    }
}
