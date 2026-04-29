package com.matheus.controle.ativos.model.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponseDTO {

    private UUID id;
    private String username;
    private String nome;
    private String role;
    private Boolean ativo;
    private LocalDateTime createdAt;
}
