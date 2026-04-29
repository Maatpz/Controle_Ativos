package com.matheus.controle.ativos.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.matheus.controle.ativos.model.Periferico;

public interface PerifericoRepository extends JpaRepository<Periferico, UUID> {

    Page<Periferico> findAll(Pageable pageable);

    @Query("SELECT p.tipo, COALESCE(SUM(p.quantidade), 0) FROM Periferico p GROUP BY p.tipo")
    java.util.List<Object[]> sumQuantidadeByTipo();
}
