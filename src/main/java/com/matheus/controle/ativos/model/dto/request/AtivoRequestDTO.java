package com.matheus.controle.ativos.model.dto.request;

import com.matheus.controle.ativos.model.enums.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtivoRequestDTO {

    @NotBlank(message = "Nome do ativo é obrigatório")
    private String nomeAtivo;

    private Status status;

    private String localidade;

    private String setor;

    private String responsavel;

    @NotBlank(message = "Patrimônio é obrigatório")
    private String patrimonio;
}
