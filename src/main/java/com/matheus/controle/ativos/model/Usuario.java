package com.matheus.controle.ativos.model;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

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

    @NotBlank(message = "Nome de usuario obrigatório")
    @Size(min = 3, max = 50, message = "Nome de usuario deve ser maior que três")
    private String username;

    @NotBlank(message = "Senha obrigatória")
    @Size(min = 4, message = "Senha deve ter no minimo 4 caracteres")
    private String password;

    private String nome;

    private Boolean ativo = true;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDate createdAt;

}
