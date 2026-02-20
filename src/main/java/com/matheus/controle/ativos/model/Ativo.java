package com.matheus.controle.ativos.model;

// import java.time.LocalDate;
import java.util.UUID;

import com.matheus.controle.ativos.model.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_ativos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ativo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome do ativo é obrigatório")
    @Column(name = "nome_ativo")
    private String nomeAtivo;

    private Status status;

    private String localidade;

    private String setor;

    private String responsavel;

    @Column(nullable = false, unique = true)
    private String patrimonio;


}