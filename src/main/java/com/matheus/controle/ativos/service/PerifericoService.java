package com.matheus.controle.ativos.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matheus.controle.ativos.exception.BusinessException;
import com.matheus.controle.ativos.exception.ResourceNotFoundException;
import com.matheus.controle.ativos.model.Periferico;
import com.matheus.controle.ativos.model.dto.request.PerifericoRequestDTO;
import com.matheus.controle.ativos.model.dto.response.PageResponseDTO;
import com.matheus.controle.ativos.model.dto.response.PerifericoResponseDTO;
import com.matheus.controle.ativos.repository.PerifericoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerifericoService {

    private final PerifericoRepository perifericoRepository;
    private final AuditoriaService auditoriaService;

    public PageResponseDTO<PerifericoResponseDTO> listar(int page, int size, String sort) {
        return PageResponseDTO.from(perifericoRepository.findAll(
                PageRequest.of(page, size, parseSort(sort)))
                .map(this::toResponseDTO));
    }

    public PerifericoResponseDTO buscarPorId(UUID id) {
        return toResponseDTO(getEntity(id));
    }

    @Transactional
    public PerifericoResponseDTO criar(PerifericoRequestDTO request) {
        Periferico periferico = new Periferico();
        apply(periferico, request);
        Periferico salvo = perifericoRepository.save(periferico);
        auditoriaService.registrar(
                "PERIFERICO",
                salvo.getId().toString(),
                "CRIACAO",
                "Periferico cadastrado: " + salvo.getNome() + " / quantidade " + salvo.getQuantidade());
        return toResponseDTO(salvo);
    }

    @Transactional
    public PerifericoResponseDTO atualizar(UUID id, PerifericoRequestDTO request) {
        Periferico periferico = getEntity(id);
        apply(periferico, request);
        Periferico salvo = perifericoRepository.save(periferico);
        auditoriaService.registrar(
                "PERIFERICO",
                salvo.getId().toString(),
                "ATUALIZACAO",
                "Periferico atualizado: " + salvo.getNome() + " / quantidade " + salvo.getQuantidade());
        return toResponseDTO(salvo);
    }

    @Transactional
    public void deletar(UUID id) {
        Periferico periferico = getEntity(id);
        perifericoRepository.delete(periferico);
        auditoriaService.registrar(
                "PERIFERICO",
                id.toString(),
                "EXCLUSAO",
                "Periferico removido: " + periferico.getNome());
    }

    private void apply(Periferico periferico, PerifericoRequestDTO request) {
        if (request.getQuantidade() == null || request.getQuantidade() < 0) {
            throw new BusinessException("Quantidade deve ser zero ou maior");
        }
        periferico.setNome(normalizeRequired(request.getNome(), "Nome do periferico"));
        periferico.setTipo(request.getTipo());
        periferico.setQuantidade(request.getQuantidade());
        periferico.setObservacoes(request.getObservacoes() != null ? request.getObservacoes().trim() : null);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(fieldName + " e obrigatorio");
        }
        return value.trim();
    }

    private Periferico getEntity(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id nao pode ser nulo");
        }
        return perifericoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Periferico nao encontrado"));
    }

    private PerifericoResponseDTO toResponseDTO(Periferico periferico) {
        return new PerifericoResponseDTO(
                periferico.getId(),
                periferico.getNome(),
                periferico.getTipo(),
                periferico.getQuantidade(),
                periferico.getObservacoes(),
                periferico.getCreatedAt(),
                periferico.getUpdatedAt());
    }

    public Map<String, Object> resumoDashboard() {
        List<Map<String, Object>> porTipo = perifericoRepository.sumQuantidadeByTipo().stream()
                .map(row -> Map.<String, Object>of(
                        "nome", row[0].toString(),
                        "total", row[1]))
                .toList();
        return Map.of("porTipo", porTipo);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "updatedAt");
        }

        List<String> allowedFields = List.of("nome", "tipo", "quantidade", "createdAt", "updatedAt");
        String[] parts = sort.split(",", 2);
        String field = allowedFields.contains(parts[0]) ? parts[0] : "updatedAt";
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}
