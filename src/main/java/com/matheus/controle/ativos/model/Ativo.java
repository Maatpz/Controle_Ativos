package com.matheus.controle.ativos.model;

import java.time.LocalDate;
import java.util.UUID;

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
    private String nome;

    private String dominio;

    private String status;

    private String localidade;

    private String setor;

    private String responsavel;

    private String licenca;

    private String categoria;

    @Column(name = "marca_fabricante")
    private String marcaFabricante;

    private String modelo;

    @Column(name = "serial_number")
    private String serialNumber;
    
    @Column(nullable = false, unique = true)
    private String patrimonio;

    private String linha;

    private String imei;

    @Column(name = "mac_ethernet")
    private String macEthernet;

    @Column(name = "mac_wifi")
    private String macWifi;

    @Column(name = "ip_fixado")
    private String ipFixado;

    private String processador;

    private String memoriaRam;

    private String armazenamento;

    @Column(name = "camera_integrada")
    private Boolean cameraIntegrada;

    @Column(name = "classificacao_info")
    private String classificacaoInfo;

    private String confidencialidade;

    private String integridade;

    private String disponibilidade;

    private String fornecedor;

    @Column(name = "data_aquisicao")
    private LocalDate dataAquisicao;

    @Column(name = "garantia_suporte")
    private LocalDate garantiaSuporte;

    @Column(name = "contato_suporte")
    private String contatoSuporte;

    

}
