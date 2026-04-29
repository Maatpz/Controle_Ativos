package com.matheus.controle.ativos.model.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaResponseDTO {

    private UUID id;
    private String entidade;
    private String entidadeId;
    private String acao;
    private String usuario;
    private String perfilUsuario;
    private String detalhes;
    private LocalDateTime createdAt;
}
