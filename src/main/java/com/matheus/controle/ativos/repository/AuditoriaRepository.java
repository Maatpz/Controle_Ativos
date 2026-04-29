package com.matheus.controle.ativos.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.matheus.controle.ativos.model.AuditoriaLog;

public interface AuditoriaRepository extends JpaRepository<AuditoriaLog, UUID> {

    Page<AuditoriaLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
