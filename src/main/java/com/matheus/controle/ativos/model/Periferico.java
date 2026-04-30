package com.matheus.controle.ativos.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.matheus.controle.ativos.model.enums.TipoPeriferico;

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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_perifericos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Periferico {

    private static final ZoneId APP_ZONE_ID = ZoneId.of("America/Sao_Paulo");

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome do periferico e obrigatorio")
    @Size(max = 120, message = "Nome do periferico deve ter no maximo 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nome;

    @NotNull(message = "Tipo do periferico e obrigatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoPeriferico tipo;

    @NotNull(message = "Quantidade e obrigatoria")
    @Min(value = 0, message = "Quantidade nao pode ser negativa")
    @Column(nullable = false)
    private Integer quantidade;

    @Size(max = 300, message = "Observacoes devem ter no maximo 300 caracteres")
    @Column(length = 300)
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
