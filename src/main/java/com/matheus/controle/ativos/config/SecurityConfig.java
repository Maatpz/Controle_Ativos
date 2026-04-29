package com.matheus.controle.ativos.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matheus.controle.ativos.service.AuditoriaService;

import jakarta.servlet.http.HttpServletResponse;
@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

        private final AuditoriaService auditoriaService;
        private final ObjectMapper objectMapper;

        @Value("${app.security.allowed-origins:http://localhost:5173,http://localhost:4173}")
        private String allowedOrigins;

        @Value("${app.security.csrf-cookie-secure:false}")
        private boolean csrfCookieSecure;

        @Value("${app.security.csrf-cookie-same-site:lax}")
        private String csrfCookieSameSite;

        public SecurityConfig(AuditoriaService auditoriaService, ObjectMapper objectMapper) {
                this.auditoriaService = auditoriaService;
                this.objectMapper = objectMapper;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(csrfTokenRepository())
                                                .ignoringRequestMatchers("/h2-console/**"))
                                .cors(cors -> {
                                })
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                                                .sessionFixation(sessionFixation -> sessionFixation.migrateSession()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/h2-console/**").permitAll()
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint((request, response, ex) -> writeJsonError(response, 401,
                                                                "Nao autenticado"))
                                                .accessDeniedHandler((request, response, ex) -> {
                                                        auditoriaService.registrar(
                                                                        "SEGURANCA",
                                                                        request.getRequestURI(),
                                                                        "ACESSO_NEGADO",
                                                                        "Tentativa negada em " + request.getMethod() + " "
                                                                                        + request.getRequestURI());
                                                        writeJsonError(response, 403, "Acesso negado");
                                                }))
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                                .formLogin(form -> form.disable())
                                .logout(logout -> logout.disable());

                return http.build();
        }

        @Bean
        public CookieCsrfTokenRepository csrfTokenRepository() {
                CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
                repository.setCookieCustomizer(builder -> builder
                                .sameSite(csrfCookieSameSite)
                                .secure(csrfCookieSecure)
                                .path("/"));
                return repository;
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                                .map(String::trim)
                                .filter(value -> !value.isEmpty())
                                .toList());
                config.setAllowCredentials(true);
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setExposedHeaders(List.of("Set-Cookie"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
                response.setStatus(status);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("message", message);
                response.getWriter().write(objectMapper.writeValueAsString(body));
        }
}
