package com.matheus.controle.ativos.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.matheus.controle.ativos.model.dto.request.PerifericoRequestDTO;
import com.matheus.controle.ativos.model.dto.response.PageResponseDTO;
import com.matheus.controle.ativos.model.dto.response.PerifericoResponseDTO;
import com.matheus.controle.ativos.service.PerifericoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/perifericos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
@org.springframework.validation.annotation.Validated
public class PerifericoController {

    private final PerifericoService perifericoService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<PerifericoResponseDTO>> listar(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort) {
        return ResponseEntity.ok(perifericoService.listar(page, size, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerifericoResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(perifericoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PerifericoResponseDTO> criar(@Valid @RequestBody PerifericoRequestDTO request) {
        return ResponseEntity.status(201).body(perifericoService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PerifericoResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PerifericoRequestDTO request) {
        return ResponseEntity.ok(perifericoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        perifericoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
