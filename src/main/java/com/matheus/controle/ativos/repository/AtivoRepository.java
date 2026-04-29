package com.matheus.controle.ativos.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.matheus.controle.ativos.model.Ativo;
import com.matheus.controle.ativos.model.enums.Status;

public interface AtivoRepository extends JpaRepository<Ativo, UUID>, JpaSpecificationExecutor<Ativo> {

    Optional<Ativo> findByPatrimonioIgnoreCase(String patrimonio);

    boolean existsByPatrimonioIgnoreCase(String patrimonio);

    Page<Ativo> findAll(Pageable pageable);

    long countByStatus(Status status);

    @Query("SELECT a.setor, COUNT(a) FROM Ativo a GROUP BY a.setor")
    java.util.List<Object[]> countGroupBySetor();

    @Query("SELECT a.categoria, COUNT(a) FROM Ativo a GROUP BY a.categoria")
    java.util.List<Object[]> countGroupByCategoria();

    @Query("SELECT a.status, COUNT(a) FROM Ativo a GROUP BY a.status")
    java.util.List<Object[]> countGroupByStatus();
}
