package com.matheus.controle.ativos.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matheus.controle.ativos.model.Ativo;
import com.matheus.controle.ativos.model.enums.Status;

public interface AtivoRepository extends JpaRepository <Ativo, UUID>{

    List<Ativo> findByNomeAtivoContainingIgnoreCase(String nomeAtivo);
    
    List<Ativo> findByResponsavelContainingIgnoreCase(String responsavel); 

    Optional<Ativo> finByPatrimonio (String patrimonio);

    List<Ativo> findByCategoriaContainingIgnoreCase(String categoria); 

    List<Ativo> findBySetorContainingIgnoreCase(String setor);

    List<Ativo> findByLocalidadeContainingIgnoreCase(String localidade); 

    List<Ativo>findByMultipleFields(String nomeAtivo, String responsavel, String patrimonio, String categoria, String setor, Status status);

}

