package com.matheus.controle.ativos.model.dto.request;

import com.matheus.controle.ativos.model.enums.Status;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtivoRequestDTO {

    @Size(max = 150, message = "Nome do ativo deve ter no maximo 150 caracteres")
    private String nomeAtivo;

    @Size(max = 100, message = "Setor deve ter no maximo 100 caracteres")
    private String setor;

    @Size(max = 100, message = "Responsavel deve ter no maximo 100 caracteres")
    private String responsavel;

    @Size(max = 100, message = "Categoria deve ter no maximo 100 caracteres")
    private String categoria;

    @jakarta.validation.constraints.NotBlank(message = "Patrimonio e obrigatorio")
    @Size(max = 50, message = "Patrimonio deve ter no maximo 50 caracteres")
    private String patrimonio;

    private Status status;

    @Size(max = 17, message = "MAC Address Ethernet deve ter no maximo 17 caracteres")
    private String macAddressEthernet;

    @Size(max = 500, message = "Observacoes devem ter no maximo 500 caracteres")
    private String observacoes;
}
