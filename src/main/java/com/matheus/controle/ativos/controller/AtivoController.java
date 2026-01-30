package com.matheus.controle.ativos.controller;

import org.springframework.web.bind.annotation.*;

import com.matheus.controle.ativos.model.Ativo;
import com.matheus.controle.ativos.model.enums.Status;
import com.matheus.controle.ativos.service.AtivoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/ativos")
public class AtivoController {

    @Autowired
    private AtivoService ativoService;

    @PostMapping
    public ResponseEntity<Ativo> createAtivo(@Valid @RequestBody Ativo ativo) {
        try {
            Ativo novoAtivo = ativoService.save(ativo);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoAtivo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }

    @GetMapping
    public ResponseEntity<List<Ativo>> getAllAtivos() {
        List<Ativo> ativos = ativoService.findAll();
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ativo> getAtivoById(@PathVariable UUID id) {
        Optional<Ativo> ativo = ativoService.findById(id);
        return ativo.map(m -> ResponseEntity.ok(m))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ativo> updateAtivo(@PathVariable UUID id, @Valid @RequestBody Ativo ativo) {
        try {
            Ativo ativoAtualizado = ativoService.updateAtivo(id, ativo);
            if (ativoAtualizado != null) {
                return ResponseEntity.ok(ativoAtualizado);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

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
    public ResponseEntity<List<Ativo>> searchAtivos(@RequestParam(required = false) String termo) {
        List<Ativo> ativos = ativoService.findByTermoGeral(termo);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/search/nome")
    public ResponseEntity<List<Ativo>> searchByNome(@RequestParam String nome) {
        List<Ativo> ativos = ativoService.findByNome(nome);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/search/responsavel")
    public ResponseEntity<List<Ativo>> searchByResponsavel(@RequestParam String responsavel) {
        List<Ativo> ativos = ativoService.findByResponsavel(responsavel);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/search/patrimonio")
    public ResponseEntity<Ativo> searchByPatrimonio(@RequestParam String patrimonio) {
        Optional<Ativo> ativo = ativoService.findByPatrimonio(patrimonio);
        return ativo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/setor")
    public ResponseEntity<List<Ativo>> searchBySetor(@RequestParam String setor) {
        List<Ativo> ativos = ativoService.findBySetor(setor);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/search/status")
    public ResponseEntity<List<Ativo>> searchByStatus(@RequestParam Status status) {
        List<Ativo> ativos = ativoService.findByStatus(status);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<List<Ativo>> searchAdvanced(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String responsavel,
            @RequestParam(required = false) String patrimonio,
            @RequestParam(required = false) String setor,
            @RequestParam(required = false) Status status) {

        List<Ativo> ativos = ativoService.findByMultipleFields(nome, responsavel, patrimonio, setor, status);
        return ResponseEntity.ok(ativos);
    }

}
