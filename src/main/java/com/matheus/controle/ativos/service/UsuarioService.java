package com.matheus.controle.ativos.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.matheus.controle.ativos.model.Usuario;
import com.matheus.controle.ativos.model.enums.Role;
import com.matheus.controle.ativos.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<Usuario> findById(UUID id) {
        if (id == null) {
            throw new RuntimeException("Id não pode ser nulo");
        }
        return usuarioRepository.findById(id);
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Optional<Usuario> findByUsernameAndAtivo(String username, Boolean ativo) {
        return usuarioRepository.findByUsernameAndAtivo(username, ativo);
    }

    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    public Usuario save(Usuario usuario) {

        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario createUsuario(String username, String password, String nome, Role role) {
        if (existsByUsername(username)) {
            throw new RuntimeException("Ja existe um usuario com esse username " + username);
        }
        Usuario usuario = new Usuario(username, password, nome, role);
        return save(usuario);

    }

    public Usuario updateUsuario(UUID id, Usuario usuarioAtualizado) {

        Optional<Usuario> usuarioExistente = findById(id);
        if (usuarioExistente.isPresent()) {

            Usuario usuario = usuarioExistente.get();

            if (usuarioAtualizado.getUsername() != null
                    && !usuarioAtualizado.getUsername().equals(usuario.getUsername())) {
                if (existsByUsername(usuarioAtualizado.getUsername())) {
                    throw new RuntimeException("Username já existe: " + usuarioAtualizado.getUsername());
                }
                usuario.setUsername(usuarioAtualizado.getUsername());

            }

            if (usuarioAtualizado.getNome() != null) {
                usuario.setNome(usuarioAtualizado.getNome());
            }

            if (usuarioAtualizado.getPassword() != null && !usuarioAtualizado.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuarioAtualizado.getPassword()));
            }

            if (usuarioAtualizado.getRole() != null) {
                usuario.setRole(usuarioAtualizado.getRole());
            }

            if (usuarioAtualizado.getAtivo() != null) {
                usuario.setAtivo(usuarioAtualizado.getAtivo());
            }
            return usuarioRepository.save(usuario);
        }
        return null;
    }

    public void deleteById(UUID id) {
        if (id == null) {
            throw new RuntimeException("Id não pode ser nulo");
        }
        usuarioRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        if (id == null) {
            throw new RuntimeException("Id não pode ser nulo");
        }
        return usuarioRepository.existsById(id);
    }

    public boolean validateCredentials(String username, String password) {
        Optional<Usuario> usuario = findByUsernameAndAtivo(username, true);
        if (usuario.isPresent()) {
            return passwordEncoder.matches(password, usuario.get().getPassword());
        }
        return false;
    }
    // Adiantando
    // Pode ser q seja usado

    @org.springframework.beans.factory.annotation.Value("${ADMIN_USERNAME:}")
    private String defaultAdminUsername;

    @org.springframework.beans.factory.annotation.Value("${ADMIN_PASSWORD:}")
    private String defaultAdminPassword;

    public void initializeDefaultAdmin() {
        if (!existsByUsername(defaultAdminUsername)) {
            createUsuario(defaultAdminUsername, defaultAdminPassword, "Admin", Role.ADMIN);
        }
    }

}
