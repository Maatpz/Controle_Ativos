package com.matheus.controle.ativos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.matheus.controle.ativos.model.Usuario;
import com.matheus.controle.ativos.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
// @CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials,
            HttpServletRequest request) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Map<String, Object> response = new HashMap<>();

        if (username == null || password == null) {
            response.put("success", false);
            response.put("message", "Username e senha são obrigatórios");
            return ResponseEntity.badRequest().body(response);
        }

        if (usuarioService.validateCredentials(username, password)) {
            Optional<Usuario> usuario = usuarioService.findByUsernameAndAtivo(username, true);

            if (usuario.isPresent()) {

                HttpSession session = request.getSession(true);
                session.setAttribute("username", username);
                session.setAttribute("userId", usuario.get().getId());
                session.setAttribute("role", usuario.get().getRole().name());

                Authentication auth = new UsernamePasswordAuthenticationToken(username, null, null);
                SecurityContextHolder.getContext().setAuthentication(auth);

                response.put("success", true);
                response.put("message", "Login realizado com sucesso");
                response.put("user", Map.of(
                        "id", usuario.get().getId(),
                        "username", usuario.get().getUsername(),
                        "nomeCompleto", usuario.get().getNome(),
                        "role", usuario.get().getRole().name()));

                return ResponseEntity.ok(response);
            }
        }

        response.put("success", false);
        response.put("message", "Credenciais inválidas");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
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
            Long userId = (Long) session.getAttribute("userId");
            String role = (String) session.getAttribute("role");

            response.put("authenticated", true);
            response.put("user", Map.of(
                    "id", userId,
                    "username", username,
                    "role", role));
        } else {
            response.put("authenticated", false);
        }

        return ResponseEntity.ok(response);
    }
}
