package com.matheus.controle.ativos.controller;

import org.springframework.web.bind.annotation.*;

import com.matheus.controle.ativos.model.dto.request.AtivoRequestDTO;
import com.matheus.controle.ativos.model.dto.response.AtivoResponseDTO;
import com.matheus.controle.ativos.model.enums.Status;
import com.matheus.controle.ativos.service.AtivoService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ativos")
@Tag(name = "Ativos", description = "API de gerenciamento de ativos")
@CrossOrigin(origins = "*")
public class AtivoController {

    @Autowired
    private AtivoService ativoService;

    @PostMapping
    public ResponseEntity<?> createAtivo(@Valid @RequestBody AtivoRequestDTO ativoRequest) {
        try {
            AtivoResponseDTO novoAtivo = ativoService.criarAtivo(ativoRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoAtivo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao criar ativo: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<AtivoResponseDTO>> getAllAtivos() {
        List<AtivoResponseDTO> ativos = ativoService.findAllDTO();
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtivoResponseDTO> getAtivoById(@PathVariable UUID id) {
        return ativoService.findByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAtivo(@PathVariable UUID id,
            @Valid @RequestBody AtivoRequestDTO ativoRequest) {
        try {
            AtivoResponseDTO ativoAtualizado = ativoService.atualizarAtivo(id, ativoRequest);
            if (ativoAtualizado != null) {
                return ResponseEntity.ok(ativoAtualizado);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao atualizar ativo: " + e.getMessage());
        }
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAtivo(@PathVariable UUID id) {
        try {
            if (ativoService.existsById(id)) {
                ativoService.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<AtivoResponseDTO>> searchAtivos(@RequestParam(required = false) String termo) {
        List<AtivoResponseDTO> ativos = ativoService.findByTermoGeralDTO(termo);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/search/nome")
    public ResponseEntity<List<AtivoResponseDTO>> searchByNome(@RequestParam String nome) {
        List<AtivoResponseDTO> ativos = ativoService.findByNomeDTO(nome);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/search/responsavel")
    public ResponseEntity<List<AtivoResponseDTO>> searchByResponsavel(@RequestParam String responsavel) {
        List<AtivoResponseDTO> ativos = ativoService.findByResponsavelDTO(responsavel);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/search/patrimonio")
    public ResponseEntity<AtivoResponseDTO> searchByPatrimonio(@RequestParam String patrimonio) {
        return ativoService.findByPatrimonioDTO(patrimonio)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/setor")
    public ResponseEntity<List<AtivoResponseDTO>> searchBySetor(@RequestParam String setor) {
        List<AtivoResponseDTO> ativos = ativoService.findBySetorDTO(setor);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/search/status")
    public ResponseEntity<List<AtivoResponseDTO>> searchByStatus(@RequestParam Status status) {
        List<AtivoResponseDTO> ativos = ativoService.findByStatusDTO(status);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<List<AtivoResponseDTO>> searchAdvanced(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String responsavel,
            @RequestParam(required = false) String patrimonio,
            @RequestParam(required = false) String setor,
            @RequestParam(required = false) Status status) {

        List<AtivoResponseDTO> ativos = ativoService.findByMultipleFieldsDTO(nome, responsavel, patrimonio, setor,
                status);
        return ResponseEntity.ok(ativos);
    }

}
