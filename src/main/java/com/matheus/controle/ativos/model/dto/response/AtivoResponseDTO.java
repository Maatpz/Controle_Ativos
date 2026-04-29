package com.matheus.controle.ativos.model.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.matheus.controle.ativos.model.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtivoResponseDTO {

    private UUID id;
    private String nomeAtivo;
    private String setor;
    private String responsavel;
    private String categoria;
    private String patrimonio;
    private Status status;
    private String macAddressEthernet;
    private String observacoes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
