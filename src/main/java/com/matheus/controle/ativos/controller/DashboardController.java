package com.matheus.controle.ativos.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matheus.controle.ativos.service.AtivoService;
import com.matheus.controle.ativos.service.PerifericoService;
import com.matheus.controle.ativos.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class DashboardController {

    private final AtivoService ativoService;
    private final PerifericoService perifericoService;
    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> resumo(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ativos", ativoService.dashboardResumo());
        response.put("perifericos", perifericoService.resumoDashboard());

        boolean isAdmin = authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (isAdmin) {
            response.put("usuarios", usuarioService.resumoUsuarios());
        }

        return ResponseEntity.ok(response);
    }
}
