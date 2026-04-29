package com.matheus.controle.ativos.model.dto.request;

import com.matheus.controle.ativos.model.enums.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateRequestDTO {

    @NotBlank(message = "Username e obrigatorio")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    private String username;

    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres")
    private String nome;

    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 6, max = 120, message = "Senha deve ter entre 6 e 120 caracteres")
    private String password;

    @NotNull(message = "Perfil e obrigatorio")
    private Role role;

    @NotNull(message = "Situacao do usuario e obrigatoria")
    private Boolean ativo;
}
