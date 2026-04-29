package com.matheus.controle.ativos.model.dto.request;

import com.matheus.controle.ativos.model.enums.TipoPeriferico;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerifericoRequestDTO {

    @NotBlank(message = "Nome do periferico e obrigatorio")
    @Size(max = 120, message = "Nome do periferico deve ter no maximo 120 caracteres")
    private String nome;

    @NotNull(message = "Tipo do periferico e obrigatorio")
    private TipoPeriferico tipo;

    @NotNull(message = "Quantidade e obrigatoria")
    @Min(value = 0, message = "Quantidade nao pode ser negativa")
    private Integer quantidade;

    @Size(max = 300, message = "Observacoes devem ter no maximo 300 caracteres")
    private String observacoes;
}
