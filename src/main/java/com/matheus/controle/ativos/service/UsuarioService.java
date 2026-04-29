package com.matheus.controle.ativos.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matheus.controle.ativos.exception.BusinessException;
import com.matheus.controle.ativos.exception.ResourceNotFoundException;
import com.matheus.controle.ativos.model.Usuario;
import com.matheus.controle.ativos.model.dto.request.UsuarioCreateRequestDTO;
import com.matheus.controle.ativos.model.dto.request.UsuarioUpdateRequestDTO;
import com.matheus.controle.ativos.model.dto.response.PageResponseDTO;
import com.matheus.controle.ativos.model.dto.response.UsuarioResponseDTO;
import com.matheus.controle.ativos.model.enums.Role;
import com.matheus.controle.ativos.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Value("${ADMIN_USERNAME:}")
    private String defaultAdminUsername;

    @Value("${ADMIN_PASSWORD:}")
    private String defaultAdminPassword;

    public PageResponseDTO<UsuarioResponseDTO> listar(int page, int size, String sort) {
        return PageResponseDTO.from(usuarioRepository.findAll(PageRequest.of(page, size, parseSort(sort)))
                .map(this::toResponseDTO));
    }

    public UsuarioResponseDTO buscarPorId(UUID id) {
        return toResponseDTO(getEntity(id));
    }

    public Optional<Usuario> findByUsernameIgnoreCaseAndAtivo(String username, Boolean ativo) {
        return usuarioRepository.findByUsernameIgnoreCaseAndAtivo(username, ativo);
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioCreateRequestDTO request) {
        String username = normalizeUsername(request.getUsername());
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("Ja existe um usuario com esse username");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setNome(normalizeNome(request.getNome()));
        usuario.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        usuario.setRole(request.getRole());
        usuario.setAtivo(Boolean.TRUE.equals(request.getAtivo()));

        Usuario salvo = usuarioRepository.save(usuario);
        auditoriaService.registrar(
                "USUARIO",
                salvo.getId().toString(),
                "CRIACAO",
                "Usuario cadastrado: " + salvo.getUsername() + " com perfil " + salvo.getRole().name());
        return toResponseDTO(salvo);
    }

    @Transactional
    public UsuarioResponseDTO atualizar(UUID id, UsuarioUpdateRequestDTO request) {
        Usuario usuario = getEntity(id);
        String username = normalizeUsername(request.getUsername());
        Optional<Usuario> usuarioComMesmoUsername = usuarioRepository.findByUsernameIgnoreCase(username);
        if (usuarioComMesmoUsername.isPresent() && !usuarioComMesmoUsername.get().getId().equals(id)) {
            throw new BusinessException("Ja existe um usuario com esse username");
        }

        Role roleAnterior = usuario.getRole();
        Boolean ativoAnterior = usuario.getAtivo();

        usuario.setUsername(username);
        usuario.setNome(normalizeNome(request.getNome()));
        usuario.setRole(request.getRole());
        usuario.setAtivo(Boolean.TRUE.equals(request.getAtivo()));
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        validateAtLeastOneAdmin(roleAnterior, ativoAnterior, usuario.getRole(), usuario.getAtivo(), usuario.getId());

        Usuario salvo = usuarioRepository.save(usuario);
        auditoriaService.registrar(
                "USUARIO",
                salvo.getId().toString(),
                "ATUALIZACAO",
                "Usuario atualizado: " + salvo.getUsername() + " com perfil " + salvo.getRole().name() + " e ativo="
                        + salvo.getAtivo());
        return toResponseDTO(salvo);
    }

    @Transactional
    public void deletar(UUID id) {
        Usuario usuario = getEntity(id);
        if (usuario.getRole() == Role.ADMIN && countActiveAdminsExcluding(id) == 0) {
            throw new BusinessException("Nao e permitido excluir o ultimo administrador ativo");
        }

        usuarioRepository.delete(usuario);
        auditoriaService.registrar(
                "USUARIO",
                id.toString(),
                "EXCLUSAO",
                "Usuario removido: " + usuario.getUsername());
    }

    public boolean validateCredentials(String username, String password) {
        Optional<Usuario> usuario = findByUsernameIgnoreCaseAndAtivo(username, true);
        return usuario.filter(value -> passwordEncoder.matches(password, value.getPassword())).isPresent();
    }

    public Map<String, Object> resumoUsuarios() {
        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("totalUsuarios", usuarioRepository.count());
        resumo.put("admins", usuarioRepository.countByRole(Role.ADMIN));
        resumo.put("users", usuarioRepository.countByRole(Role.USER));
        return resumo;
    }

    public void initializeDefaultAdmin() {
        if (defaultAdminUsername == null || defaultAdminUsername.isBlank()) {
            return;
        }

        Optional<Usuario> usuarioExistente = usuarioRepository.findByUsernameIgnoreCase(defaultAdminUsername.trim());
        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            usuario.setAtivo(true);
            usuario.setRole(Role.ADMIN);
            if (defaultAdminPassword != null && !defaultAdminPassword.isBlank()
                    && !passwordEncoder.matches(defaultAdminPassword, usuario.getPassword())) {
                usuario.setPassword(passwordEncoder.encode(defaultAdminPassword));
            }
            usuarioRepository.save(usuario);
            return;
        }

        Usuario admin = new Usuario();
        admin.setUsername(normalizeUsername(defaultAdminUsername));
        admin.setNome("Administrador");
        admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
        admin.setRole(Role.ADMIN);
        admin.setAtivo(true);
        usuarioRepository.save(admin);
    }

    private Usuario getEntity(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id nao pode ser nulo");
        }
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNome(),
                usuario.getRole().name(),
                usuario.getAtivo(),
                usuario.getCreatedAt());
    }

    private String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("Username e obrigatorio");
        }
        return username.trim().toLowerCase();
    }

    private String normalizeNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new BusinessException("Nome e obrigatorio");
        }
        return nome.trim();
    }

    private void validateAtLeastOneAdmin(Role oldRole, Boolean oldAtivo, Role newRole, Boolean newAtivo, UUID currentId) {
        boolean eraAdminAtivo = oldRole == Role.ADMIN && Boolean.TRUE.equals(oldAtivo);
        boolean continuaAdminAtivo = newRole == Role.ADMIN && Boolean.TRUE.equals(newAtivo);

        if (eraAdminAtivo && !continuaAdminAtivo && countActiveAdminsExcluding(currentId) == 0) {
            throw new BusinessException("Nao e permitido remover o ultimo administrador ativo");
        }
    }

    private long countActiveAdminsExcluding(UUID excludedId) {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> !usuario.getId().equals(excludedId))
                .filter(usuario -> usuario.getRole() == Role.ADMIN)
                .filter(usuario -> Boolean.TRUE.equals(usuario.getAtivo()))
                .count();
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        java.util.List<String> allowedFields = java.util.List.of("username", "nome", "role", "ativo", "createdAt", "updatedAt");
        String[] parts = sort.split(",", 2);
        String field = allowedFields.contains(parts[0]) ? parts[0] : "createdAt";
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}
