package com.matheus.controle.ativos.model;

// import java.time.LocalDate;
import java.util.UUID;

import com.matheus.controle.ativos.model.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    // private String dominio;
    @Enumerated(EnumType.STRING)
    private Status status;

    private String localidade;

    private String setor;

    private String responsavel;

    // private String licenca;

    // private String categoria;

    // @Column(name = "marca_fabricante")
    // private String marcaFabricante;

    // private String modelo;

    // @Column(name = "serial_number")
    // private String serialNumber;
    
    @Column(nullable = false, unique = true)
    private String patrimonio;

    // private String linha;

    // private String imei;

    // @Column(name = "mac_ethernet")
    // private String macEthernet;

    // @Column(name = "mac_wifi")
    // private String macWifi;

    // @Column(name = "ip_fixado")
    // private String ipFixado;

    // private String processador;

    // private String memoriaRam;

    // private String armazenamento;

    // @Column(name = "camera_integrada")
    // private Boolean cameraIntegrada;

    // @Column(name = "classificacao_info")
    // private String classificacaoInfo;

    // private String confidencialidade;

    // private String integridade;

    // private String disponibilidade;

    // private String fornecedor;

    // @Column(name = "data_aquisicao")
    // private LocalDate dataAquisicao;

    // @Column(name = "garantia_suporte")
    // private LocalDate garantiaSuporte;

    // @Column(name = "contato_suporte")
    // private String contatoSuporte;

    // @Column(name = "nota_fiscal")
    // private String notaFiscal;
    
    // @Column(name = "resgistro_mudanca")
    // private String registroMudanca;

    // @Column(name = "data_ultima_avaliacao")
    // private LocalDate dataUltimaAvaliacao;
    
    // @Column(name = "proxima_avaliacao")
    // private LocalDate proximaAvaliacao;
    
    // private Boolean bitlocker;

    // @Column(name = "antivirus_licenca")
    // private String antivirusLicenca;

    // @Column(name = "antivirus_versao")
    // private String antivirusVersao;

    // @Column(name = "bart_wazuh")
    // private String bartWazuh;

    // @Column(name = "chrome_enterprise")
    // private String chromeEnterprise;

    // @Column(name = "acesso_remoto_id")
    // private String acessoRemotoId;

    // @Column(name = "acesso_remoto")
    // private String acessoRemoto;

    // @Column(name = "termo_custodia")
    // private String termoCustodia;

    // @Column(name = "senha_admin")
    // private String senhaAdmin;

    // @Column(name = "bloqueio_tela")
    // private String bloqueioTela;

    // @Column(name = "conta_atribuida")
    // private String contaAtribuida;

    // @Column(name = "senha_conta_atribuida")
    // private String senhaContaAtribuida;

    // @Column(name = "contrato_procedimentos")
    // private String contratoProcedimentos;

    // @Column(name = "itens_configuracao")
    // private String itensConfiguracao;

    // @Column(name = "manutencao_preventiva")
    // private String manutencaoPreventiva;

    // @Column(columnDefinition = "TEXT")
    // private String obs;

}


// {
//   "ADMIN_PASSWORD": "infra!@#357",
//   "ADMIN_USERNAME": "infra",
//   "DATABASE_PASSWORD": "npg_ZU5Bd6rsKvnS",
//   "DATABASE_URL": "jdbc:postgresql://ep-divine-fog-acm0f74m-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require",
//   "DATABASE_USERNAME": "neondb_owner"
// }