package com.matheus.controle.ativos.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaLog {

    private static final ZoneId APP_ZONE_ID = ZoneId.of("America/Sao_Paulo");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 40)
    private String entidade;

    @Column(name = "entidade_id", length = 80)
    private String entidadeId;

    @Column(nullable = false, length = 40)
    private String acao;

    @Column(nullable = false, length = 80)
    private String usuario;

    @Column(name = "perfil_usuario", nullable = false, length = 40)
    private String perfilUsuario;

    @Column(nullable = false, length = 1000)
    private String detalhes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(APP_ZONE_ID);
        }
    }
}
