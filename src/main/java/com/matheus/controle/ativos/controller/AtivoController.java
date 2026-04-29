package com.matheus.controle.ativos.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
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

import com.matheus.controle.ativos.model.dto.request.AtivoRequestDTO;
import com.matheus.controle.ativos.model.dto.response.AtivoResponseDTO;
import com.matheus.controle.ativos.model.dto.response.PageResponseDTO;
import com.matheus.controle.ativos.service.AtivoService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ativos")
@Tag(name = "Ativos", description = "API de gerenciamento de ativos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
@org.springframework.validation.annotation.Validated
public class AtivoController {

    private final AtivoService ativoService;

    @PostMapping
    public ResponseEntity<AtivoResponseDTO> createAtivo(@Valid @RequestBody AtivoRequestDTO ativoRequest) {
        return ResponseEntity.status(201).body(ativoService.criarAtivo(ativoRequest));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<AtivoResponseDTO>> getAllAtivos(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort,
            @RequestParam(required = false) @Size(max = 100) String termo,
            @RequestParam(required = false) @Size(max = 100) String nome,
            @RequestParam(required = false) @Size(max = 100) String responsavel,
            @RequestParam(required = false) @Size(max = 50) String patrimonio) {
        return ResponseEntity.ok(ativoService.listar(page, size, sort, termo, nome, responsavel, patrimonio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtivoResponseDTO> getAtivoById(@PathVariable UUID id) {
        return ResponseEntity.ok(ativoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtivoResponseDTO> updateAtivo(
            @PathVariable UUID id,
            @Valid @RequestBody AtivoRequestDTO ativoRequest) {
        return ResponseEntity.ok(ativoService.atualizarAtivo(id, ativoRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAtivo(@PathVariable UUID id) {
        ativoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<AtivoResponseDTO>> searchAtivos(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort,
            @RequestParam(required = false) @Size(max = 100) String termo) {
        return ResponseEntity.ok(ativoService.listar(page, size, sort, termo, null, null, null));
    }

    @GetMapping(value = "/export/txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> exportarTxt(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String responsavel,
            @RequestParam(required = false) String patrimonio) {
        List<AtivoResponseDTO> ativos = ativoService.listarParaExportacao(termo, nome, responsavel, patrimonio);
        String conteudo = ativoService.exportarTxt(ativos);
        String filename = "ativos-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".txt";
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
                .body(conteudo);
    }
}