package com.matheus.controle.ativos.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.matheus.controle.ativos.model.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "tb_usuarios")
@Data
public class Usuario {

    public Usuario(String username, String password, String nome, Role role) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.role = role;
    }

    public Usuario() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome de usuario obrigatorio")
    @Size(min = 3, max = 50, message = "Nome de usuario deve ter entre 3 e 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Senha obrigatoria")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Nome obrigatorio")
    @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
