package com.matheus.controle.ativos.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.matheus.controle.ativos.model.AuditoriaLog;
import com.matheus.controle.ativos.model.Usuario;
import com.matheus.controle.ativos.model.dto.response.AuditoriaResponseDTO;
import com.matheus.controle.ativos.model.dto.response.PageResponseDTO;
import com.matheus.controle.ativos.repository.AuditoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public void registrar(String entidade, String entidadeId, String acao, String detalhes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usuario = "sistema";
        String perfil = "SISTEMA";

        if (authentication != null && authentication.isAuthenticated()) {
            usuario = authentication.getName();
            perfil = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .map(authority -> authority.replace("ROLE_", ""))
                    .orElse("USER");
        }

        salvar(entidade, entidadeId, acao, usuario, perfil, detalhes);
    }

    public void registrarAutenticacao(String acao, Usuario usuario, String detalhes) {
        salvar(
                "AUTENTICACAO",
                usuario != null && usuario.getId() != null ? usuario.getId().toString() : null,
                acao,
                usuario != null ? usuario.getUsername() : "anonimo",
                usuario != null && usuario.getRole() != null ? usuario.getRole().name() : "ANONIMO",
                detalhes);
    }

    public PageResponseDTO<AuditoriaResponseDTO> listarRecentes(int page, int size) {
        int safeSize = Math.max(1, Math.min(size, 200));
        Page<AuditoriaResponseDTO> response = auditoriaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, safeSize))
                .map(this::toResponse);
        return PageResponseDTO.from(response);
    }

    private void salvar(String entidade, String entidadeId, String acao, String usuario, String perfil, String detalhes) {
        AuditoriaLog log = new AuditoriaLog();
        log.setEntidade(entidade);
        log.setEntidadeId(entidadeId);
        log.setAcao(acao);
        log.setUsuario(usuario);
        log.setPerfilUsuario(perfil);
        log.setDetalhes(detalhes);
        auditoriaRepository.save(log);
    }

    private AuditoriaResponseDTO toResponse(AuditoriaLog log) {
        return new AuditoriaResponseDTO(
                log.getId(),
                log.getEntidade(),
                log.getEntidadeId(),
                log.getAcao(),
                log.getUsuario(),
                log.getPerfilUsuario(),
                log.getDetalhes(),
                log.getCreatedAt());
    }
}
