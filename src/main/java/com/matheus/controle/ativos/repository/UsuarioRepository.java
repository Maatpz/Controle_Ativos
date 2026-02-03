package com.matheus.controle.ativos.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matheus.controle.ativos.model.Usuario;

public interface UsuarioRepository extends JpaRepository <Usuario, UUID> {

    // Optional<Usuario> findById (UUID id);

    Optional<Usuario> findByUsername (String username);

    boolean existsByUsername(String username);

    Optional<Usuario> findByUsernameAndAtivo(String username, Boolean Ativo);
    
}
