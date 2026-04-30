package com.matheus.controle.ativos.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.matheus.controle.ativos.model.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_ativos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ativo {

    private static final ZoneId APP_ZONE_ID = ZoneId.of("America/Sao_Paulo");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(max = 150, message = "Nome do ativo deve ter no maximo 150 caracteres")
    @Column(name = "nome_ativo", length = 150)
    private String nomeAtivo;

    @Size(max = 100, message = "Setor deve ter no maximo 100 caracteres")
    @Column(length = 100)
    private String setor;

    @Size(max = 100, message = "Responsavel deve ter no maximo 100 caracteres")
    @Column(length = 100)
    private String responsavel;

    @Size(max = 100, message = "Categoria deve ter no maximo 100 caracteres")
    @Column(length = 100)
    private String categoria;

    @jakarta.validation.constraints.NotBlank(message = "Patrimonio e obrigatorio")
    @Size(max = 50, message = "Patrimonio deve ter no maximo 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String patrimonio;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Status status;

    @Size(max = 17, message = "MAC Address Ethernet deve ter no maximo 17 caracteres")
    @Column(name = "mac_address_ethernet", length = 17)
    private String macAddressEthernet;

    @Size(max = 500, message = "Observacoes devem ter no maximo 500 caracteres")
    @Column(length = 500)
    private String observacoes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now(APP_ZONE_ID);
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now(APP_ZONE_ID);
    }
}
