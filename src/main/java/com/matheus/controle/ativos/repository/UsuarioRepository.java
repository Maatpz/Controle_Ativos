package com.matheus.controle.ativos.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.matheus.controle.ativos.model.Usuario;
import com.matheus.controle.ativos.model.enums.Role;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    Optional<Usuario> findByUsernameIgnoreCaseAndAtivo(String username, Boolean ativo);

    boolean existsByUsernameIgnoreCase(String username);

    Page<Usuario> findAll(Pageable pageable);

    long countByRole(Role role);

    long countByRoleAndAtivoTrue(Role role);
}
