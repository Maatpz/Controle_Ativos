package com.matheus.controle.ativos.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.matheus.controle.ativos.model.Usuario;
import com.matheus.controle.ativos.model.dto.request.LoginRequestDTO;
import com.matheus.controle.ativos.model.dto.response.LoginResponseDTO;
import com.matheus.controle.ativos.service.AuditoriaService;
import com.matheus.controle.ativos.service.UsuarioService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacao", description = "API de autenticacao")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO credentials,
            HttpServletRequest request) {
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        if (!usuarioService.validateCredentials(username, password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponseDTO.falha("Credenciais invalidas"));
        }

        Optional<Usuario> usuarioOpt = usuarioService.findByUsernameIgnoreCaseAndAtivo(username, true);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponseDTO.falha("Usuario inativo ou inexistente"));
        }

        Usuario usuario = usuarioOpt.get();
        String normalizedUsername = usuario.getUsername();

        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            request.changeSessionId();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("username", normalizedUsername);
        session.setAttribute("userId", usuario.getId());
        session.setAttribute("role", usuario.getRole().name());
        session.setAttribute("nome", usuario.getNome());

        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = java.util.List.of(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + usuario.getRole().name()));

        Authentication auth = new UsernamePasswordAuthenticationToken(normalizedUsername, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(auth));

        auditoriaService.registrarAutenticacao(
                "LOGIN",
                usuario,
                "Login realizado com sucesso");

        return ResponseEntity.ok(LoginResponseDTO.sucesso(
                "Login realizado com sucesso",
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNome(),
                usuario.getRole().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String username = null;
        if (session != null) {
            Object usernameAttr = session.getAttribute("username");
            if (usernameAttr != null) {
                username = usernameAttr.toString();
            }
        }

        if (username != null) {
            usuarioService.findByUsernameIgnoreCaseAndAtivo(username, true)
                    .ifPresent(usuario -> auditoriaService.registrarAutenticacao(
                            "LOGOUT",
                            usuario,
                            "Logout realizado com sucesso"));
        }

        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout realizado com sucesso");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> response = new HashMap<>();

        if (session != null && session.getAttribute("username") != null) {
            response.put("authenticated", true);

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", session.getAttribute("userId"));
            userData.put("username", session.getAttribute("username"));
            userData.put("role", session.getAttribute("role"));
            userData.put("nome", session.getAttribute("nome"));
            response.put("user", userData);
            return ResponseEntity.ok(response);
        }

        response.put("authenticated", false);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, Object>> getCsrfToken(CsrfToken csrfToken) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", csrfToken.getToken());
        response.put("headerName", csrfToken.getHeaderName());
        response.put("parameterName", csrfToken.getParameterName());
        return ResponseEntity.ok(response);
    }
}
