package com.matheus.controle.ativos.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matheus.controle.ativos.exception.BusinessException;
import com.matheus.controle.ativos.exception.ResourceNotFoundException;
import com.matheus.controle.ativos.model.Ativo;
import com.matheus.controle.ativos.model.dto.request.AtivoRequestDTO;
import com.matheus.controle.ativos.model.dto.response.AtivoResponseDTO;
import com.matheus.controle.ativos.model.dto.response.PageResponseDTO;
import com.matheus.controle.ativos.model.enums.Status;
import com.matheus.controle.ativos.repository.AtivoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AtivoService {

    private static final int MAX_EXPORT_SIZE = 1000;

    private final AtivoRepository ativoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional
    public AtivoResponseDTO criarAtivo(AtivoRequestDTO request) {
        String patrimonio = normalizePatrimonio(request.getPatrimonio());
        if (ativoRepository.existsByPatrimonioIgnoreCase(patrimonio)) {
            throw new BusinessException("Ja existe um ativo com o patrimonio informado");
        }

        Ativo ativo = new Ativo();
        apply(ativo, request, patrimonio);
        Ativo salvo = ativoRepository.save(ativo);
        auditoriaService.registrar(
                "ATIVO",
                salvo.getId().toString(),
                "CRIACAO",
                "Ativo cadastrado: " + resumo(salvo));
        return toResponseDTO(salvo);
    }

    @Transactional
    public AtivoResponseDTO atualizarAtivo(UUID id, AtivoRequestDTO request) {
        Ativo ativo = getEntity(id);
        String patrimonio = normalizePatrimonio(request.getPatrimonio());
        Optional<Ativo> ativoComMesmoPatrimonio = ativoRepository.findByPatrimonioIgnoreCase(patrimonio);
        if (ativoComMesmoPatrimonio.isPresent() && !ativoComMesmoPatrimonio.get().getId().equals(id)) {
            throw new BusinessException("Ja existe um ativo com o patrimonio informado");
        }

        String detalhesAlteracao = buildChangeLog(ativo, request, patrimonio);
        apply(ativo, request, patrimonio);
        Ativo salvo = ativoRepository.save(ativo);
        auditoriaService.registrar(
                "ATIVO",
                salvo.getId().toString(),
                "ATUALIZACAO",
                detalhesAlteracao);
        return toResponseDTO(salvo);
    }

    public PageResponseDTO<AtivoResponseDTO> listar(
            int page,
            int size,
            String sort,
            String termo,
            String nome,
            String responsavel,
            String patrimonio) {
        Page<AtivoResponseDTO> ativos = ativoRepository.findAll(
                buildSpecification(termo, nome, responsavel, patrimonio),
                buildPageRequest(page, size, sort))
                .map(this::toResponseDTO);
        return PageResponseDTO.from(ativos);
    }

    public AtivoResponseDTO buscarPorId(UUID id) {
        return toResponseDTO(getEntity(id));
    }

    public List<AtivoResponseDTO> listarParaExportacao(String termo, String nome, String responsavel, String patrimonio) {
        if (!hasText(termo) && !hasText(nome) && !hasText(responsavel) && !hasText(patrimonio)) {
            throw new BusinessException("Informe ao menos um filtro para exportar ativos");
        }

        Page<Ativo> ativos = ativoRepository.findAll(
                buildSpecification(termo, nome, responsavel, patrimonio),
                PageRequest.of(0, MAX_EXPORT_SIZE + 1, Sort.by(Sort.Direction.DESC, "updatedAt")));
        if (ativos.getTotalElements() > MAX_EXPORT_SIZE) {
            throw new BusinessException("Exportacao limitada a " + MAX_EXPORT_SIZE + " ativos por vez");
        }

        return ativos.getContent().stream().map(this::toResponseDTO).toList();
    }

    @Transactional
    public void deletar(UUID id) {
        Ativo ativo = getEntity(id);
        ativoRepository.delete(ativo);
        auditoriaService.registrar(
                "ATIVO",
                id.toString(),
                "EXCLUSAO",
                "Ativo removido: " + resumo(ativo));
    }

    public Map<String, Object> dashboardResumo() {
        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("totalAtivos", ativoRepository.count());
        resumo.put("operacionais", ativoRepository.countByStatus(Status.OPERACIONAL));
        resumo.put("estoque", ativoRepository.countByStatus(Status.ESTOQUE));
        resumo.put("manutencao", ativoRepository.countByStatus(Status.MANUTENCAO));
        resumo.put("porSetor", agruparPorTexto(ativoRepository.countGroupBySetor(), false));
        resumo.put("porCategoria", agruparPorTexto(ativoRepository.countGroupByCategoria(), false));
        resumo.put("porStatus", agruparPorTexto(ativoRepository.countGroupByStatus(), true));
        return resumo;
    }

    public String exportarTxt(List<AtivoResponseDTO> ativos) {
        StringBuilder sb = new StringBuilder();
        sb.append("CONTROLE DE ATIVOS").append(System.lineSeparator());
        sb.append("Total: ").append(ativos.size()).append(" ativo(s)").append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Nome | Patrimonio | Status | Responsavel | Setor | Categoria").append(System.lineSeparator());

        for (AtivoResponseDTO ativo : ativos) {
            sb.append(ativo.getNomeAtivo()).append(" | ")
                    .append(ativo.getPatrimonio()).append(" | ")
                    .append(ativo.getStatus()).append(" | ")
                    .append(ativo.getResponsavel()).append(" | ")
                    .append(ativo.getSetor()).append(" | ")
                    .append(ativo.getCategoria())
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }

    private Ativo getEntity(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id nao pode ser nulo");
        }
        return ativoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ativo nao encontrado"));
    }

    private void apply(Ativo ativo, AtivoRequestDTO request, String patrimonioNormalizado) {
        ativo.setNomeAtivo(normalizeOptional(request.getNomeAtivo()));
        ativo.setSetor(normalizeOptional(request.getSetor()));
        ativo.setResponsavel(normalizeOptional(request.getResponsavel()));
        ativo.setCategoria(normalizeOptional(request.getCategoria()));
        ativo.setPatrimonio(patrimonioNormalizado);
        ativo.setStatus(normalizeStatus(request.getStatus()));
        ativo.setMacAddressEthernet(normalizeOptional(request.getMacAddressEthernet()));
        ativo.setObservacoes(normalizeOptional(request.getObservacoes()));
    }

    private AtivoResponseDTO toResponseDTO(Ativo ativo) {
        return new AtivoResponseDTO(
                ativo.getId(),
                ativo.getNomeAtivo(),
                ativo.getSetor(),
                ativo.getResponsavel(),
                ativo.getCategoria(),
                ativo.getPatrimonio(),
                ativo.getStatus(),
                ativo.getMacAddressEthernet(),
                ativo.getObservacoes(),
                ativo.getCreatedAt(),
                ativo.getUpdatedAt());
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!hasText(value)) {
            throw new BusinessException(fieldName + " e obrigatorio");
        }
        return value.trim();
    }

    private String normalizePatrimonio(String patrimonio) {
        return normalizeRequired(patrimonio, "Patrimonio").toUpperCase();
    }

    private String normalizeNullable(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String normalizeOptional(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String labelOrDefault(String value) {
        return hasText(value) ? value.trim() : "Nao informado";
    }

    private com.matheus.controle.ativos.model.enums.Status normalizeStatus(
            com.matheus.controle.ativos.model.enums.Status status) {
        return status != null ? status : com.matheus.controle.ativos.model.enums.Status.OPERACIONAL;
    }

    private String buildChangeLog(Ativo atual, AtivoRequestDTO request, String patrimonioNormalizado) {
        List<String> changes = new ArrayList<>();
        addIfChanged(changes, "nomeAtivo", atual.getNomeAtivo(), normalizeOptional(request.getNomeAtivo()));
        addIfChanged(changes, "setor", atual.getSetor(), normalizeOptional(request.getSetor()));
        addIfChanged(changes, "responsavel", atual.getResponsavel(), normalizeOptional(request.getResponsavel()));
        addIfChanged(changes, "categoria", atual.getCategoria(), normalizeOptional(request.getCategoria()));
        addIfChanged(changes, "patrimonio", atual.getPatrimonio(), patrimonioNormalizado);
        addIfChanged(changes, "status", atual.getStatus() == null ? null : atual.getStatus().name(),
                normalizeStatus(request.getStatus()).name());
        addIfChanged(changes, "macAddressEthernet", atual.getMacAddressEthernet(),
                normalizeOptional(request.getMacAddressEthernet()));
        addIfChanged(changes, "observacoes", atual.getObservacoes(), normalizeOptional(request.getObservacoes()));

        if (changes.isEmpty()) {
            return "Atualizacao sem alteracao efetiva nos dados do ativo " + atual.getPatrimonio();
        }

        return "Ativo atualizado (" + atual.getPatrimonio() + "): " + String.join("; ", changes);
    }

    private void addIfChanged(List<String> changes, String field, String oldValue, String newValue) {
        String before = oldValue == null ? "" : oldValue;
        String after = newValue == null ? "" : newValue;
        if (!before.equals(after)) {
            changes.add(field + ": '" + before + "' -> '" + after + "'");
        }
    }

    private String resumo(Ativo ativo) {
        String nome = hasText(ativo.getNomeAtivo()) ? ativo.getNomeAtivo() : "Sem nome";
        String status = ativo.getStatus() != null ? ativo.getStatus().name() : "SEM_STATUS";
        return nome + " / patrimonio " + ativo.getPatrimonio() + " / status " + status;
    }

    private List<Map<String, Object>> agruparPorTexto(List<Object[]> rows, boolean enumValue) {
        return rows.stream()
                .map(row -> {
                    String nome = row[0] == null
                            ? "Nao informado"
                            : enumValue ? row[0].toString() : labelOrDefault(row[0].toString());
                    Long total = row[1] instanceof Long ? (Long) row[1] : Long.valueOf(String.valueOf(row[1]));
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("nome", nome);
                    item.put("total", total);
                    return item;
                })
                .sorted((left, right) -> String.valueOf(left.get("nome")).compareToIgnoreCase(String.valueOf(right.get("nome"))))
                .toList();
    }

    private Specification<Ativo> buildSpecification(String termo, String nome, String responsavel, String patrimonio) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (hasText(termo)) {
                String value = "%" + termo.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nomeAtivo")), value),
                        cb.like(cb.lower(root.get("responsavel")), value),
                        cb.like(cb.lower(root.get("patrimonio")), value)));
            }
            if (hasText(nome)) {
                predicates.add(cb.like(cb.lower(root.get("nomeAtivo")), "%" + nome.trim().toLowerCase() + "%"));
            }
            if (hasText(responsavel)) {
                predicates.add(cb.like(cb.lower(root.get("responsavel")), "%" + responsavel.trim().toLowerCase() + "%"));
            }
            if (hasText(patrimonio)) {
                predicates.add(cb.like(cb.lower(root.get("patrimonio")), "%" + patrimonio.trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private PageRequest buildPageRequest(int page, int size, String sort) {
        return PageRequest.of(page, size, parseSort(sort,
                List.of("nomeAtivo", "patrimonio", "responsavel", "setor", "categoria", "status", "createdAt", "updatedAt"),
                "updatedAt"));
    }

    private Sort parseSort(String sort, List<String> allowedFields, String defaultField) {
        if (!hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, defaultField);
        }

        String[] parts = sort.split(",", 2);
        String field = allowedFields.contains(parts[0]) ? parts[0] : defaultField;
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}
