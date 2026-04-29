package com.matheus.controle.ativos.model.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.matheus.controle.ativos.model.enums.TipoPeriferico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerifericoResponseDTO {

    private UUID id;
    private String nome;
    private TipoPeriferico tipo;
    private Integer quantidade;
    private String observacoes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
