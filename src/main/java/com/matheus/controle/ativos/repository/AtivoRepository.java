package com.matheus.controle.ativos.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.matheus.controle.ativos.model.Ativo;
import com.matheus.controle.ativos.model.enums.Status;

public interface AtivoRepository extends JpaRepository<Ativo, UUID> {

        List<Ativo> findByNomeAtivoContainingIgnoreCase(String nomeAtivo);

        List<Ativo> findByResponsavelContainingIgnoreCase(String responsavel);

        Optional<Ativo> findByPatrimonio(String patrimonio);

        // List<Ativo> findByCategoriaContainingIgnoreCase(String categoria);

        List<Ativo> findBySetorContainingIgnoreCase(String setor);

        List<Ativo> findByLocalidadeContainingIgnoreCase(String localidade);

        List<Ativo> findByStatus(Status status);

        @Query("SELECT a FROM Ativo a WHERE " +
                        "(:nomeAtivo IS NULL OR LOWER(a.nomeAtivo) LIKE LOWER(CONCAT('%', :nomeAtivo, '%'))) AND " +
                        "(:responsavel IS NULL OR LOWER(a.responsavel) LIKE LOWER(CONCAT('%', :responsavel, '%'))) AND " +
                        "(:patrimonio IS NULL OR LOWER(a.patrimonio) LIKE LOWER(CONCAT('%', :patrimonio, '%'))) AND " +
                        "(:setor IS NULL OR LOWER(a.setor) LIKE LOWER(CONCAT('%', :setor, '%'))) AND " +
                        "(:status IS NULL OR a.status = :status)")
        List<Ativo> findByMultipleFields(@Param("nomeAtivo") String nomeAtivo,
                        @Param("responsavel") String responsavel,
                        @Param("patrimonio") String patrimonio,
                        @Param("setor") String setor,
                        @Param("status") Status status);

        @Query("SELECT a FROM Ativo a WHERE " +
                        "LOWER(a.nomeAtivo) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
                        "LOWER(a.responsavel) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
                        "LOWER(a.patrimonio) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
                        "LOWER(a.setor) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
                        "LOWER(a.localidade) LIKE LOWER(CONCAT('%', :termo, '%'))")
        List<Ativo> findByTermoGeral(@Param("termo") String termo);
}
