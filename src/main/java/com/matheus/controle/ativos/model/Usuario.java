package com.matheus.controle.ativos.model;

import java.util.UUID;

import com.matheus.controle.ativos.model.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name= "tb_usuarios")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome de usuario obrigatório")
    @Size(min = 3, max = 50, message = "Nome de usuario deve ser maior que três")
    @Column(nullable = false)
    private String username;

    @NotBlank(message = "Senha obrigatória")
    @Size(min = 6, message = "Senha deve ter no minimo 6 caracteres")
    @Column
    private String password;

    private String nome;

    private Boolean ativo = true;

    private Role role = Role.ADMIN;
    
}
