package com.matheus.controle.ativos.model.dto.response;

import com.matheus.controle.ativos.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtivoResponseDTO {

    private UUID id;
    private String nomeAtivo;
    private Status status;
    private String localidade;
    private String setor;
    private String responsavel;
    private String patrimonio;
}
