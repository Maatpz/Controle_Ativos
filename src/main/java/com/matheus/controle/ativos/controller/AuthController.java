package com.matheus.controle.ativos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import com.matheus.controle.ativos.model.Usuario;
import com.matheus.controle.ativos.model.dto.request.LoginRequestDTO;
import com.matheus.controle.ativos.model.dto.response.LoginResponseDTO;
import com.matheus.controle.ativos.service.UsuarioService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "API de autenticação")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO credentials,
            HttpServletRequest request) {
        String username = credentials.getUsername();
        String password = credentials.getPassword();

        if (usuarioService.validateCredentials(username, password)) {
            Optional<Usuario> usuarioOpt = usuarioService.findByUsernameAndAtivo(username, true);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                HttpSession session = request.getSession(true);
                session.setAttribute("username", username);
                session.setAttribute("userId", usuario.getId());
                session.setAttribute("role", usuario.getRole().name());

                java.util.List<org.springframework.security.core.GrantedAuthority> authorities = java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + usuario.getRole().name()));

                Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        new SecurityContextImpl(auth));

                return ResponseEntity.ok(LoginResponseDTO.sucesso(
                        "Login realizado com sucesso",
                        usuario.getId(),
                        usuario.getUsername(),
                        usuario.getNome(),
                        usuario.getRole().name()));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(LoginResponseDTO.falha("Credenciais inválidas"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout realizado com sucesso");

        return ResponseEntity.ok(response);
    }

    // Endpoint para verificar status de autenticação
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> response = new HashMap<>();

        if (session != null && session.getAttribute("username") != null) {
            String username = (String) session.getAttribute("username");
            Object userIdAttr = session.getAttribute("userId");
            String role = (String) session.getAttribute("role");
            String userId = userIdAttr != null ? userIdAttr.toString() : null;

            response.put("authenticated", true);
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", userId);
            userData.put("username", username);
            userData.put("role", role);
            response.put("user", userData);
        } else {
            response.put("authenticated", false);
        }

        return ResponseEntity.ok(response);
    }
}
