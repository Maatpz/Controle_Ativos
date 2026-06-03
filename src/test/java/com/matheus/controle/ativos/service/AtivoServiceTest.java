package com.matheus.controle.ativos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.matheus.controle.ativos.exception.BusinessException;
import com.matheus.controle.ativos.model.Ativo;
import com.matheus.controle.ativos.model.dto.request.AtivoRequestDTO;
import com.matheus.controle.ativos.model.dto.response.AtivoResponseDTO;
import com.matheus.controle.ativos.model.enums.Status;
import com.matheus.controle.ativos.repository.AtivoRepository;

@ExtendWith(MockitoExtension.class)
class AtivoServiceTest {

    @Mock
    private AtivoRepository ativoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private AtivoService ativoService;

    @Test
    void criarAtivoDeveNormalizarPatrimonioSalvarERetornarResponse() {
        AtivoRequestDTO request = new AtivoRequestDTO(
                " Notebook Dell ",
                " TI ",
                " Matheus ",
                " Notebook ",
                " pat-001 ",
                Status.ESTOQUE,
                "AA:BB:CC:DD:EE:FF",
                " Equipamento novo ");

        when(ativoRepository.existsByPatrimonioIgnoreCase("PAT-001")).thenReturn(false);
        when(ativoRepository.save(any(Ativo.class))).thenAnswer(invocation -> {
            Ativo ativo = invocation.getArgument(0);
            ativo.setId(UUID.randomUUID());
            return ativo;
        });

        AtivoResponseDTO response = ativoService.criarAtivo(request);

        ArgumentCaptor<Ativo> ativoCaptor = ArgumentCaptor.forClass(Ativo.class);
        verify(ativoRepository).save(ativoCaptor.capture());
        Ativo ativoSalvo = ativoCaptor.getValue();

        assertThat(ativoSalvo.getNomeAtivo()).isEqualTo("Notebook Dell");
        assertThat(ativoSalvo.getSetor()).isEqualTo("TI");
        assertThat(ativoSalvo.getResponsavel()).isEqualTo("Matheus");
        assertThat(ativoSalvo.getCategoria()).isEqualTo("Notebook");
        assertThat(ativoSalvo.getPatrimonio()).isEqualTo("PAT-001");
        assertThat(ativoSalvo.getStatus()).isEqualTo(Status.ESTOQUE);
        assertThat(ativoSalvo.getObservacoes()).isEqualTo("Equipamento novo");

        assertThat(response.getId()).isNotNull();
        assertThat(response.getPatrimonio()).isEqualTo("PAT-001");
        assertThat(response.getStatus()).isEqualTo(Status.ESTOQUE);

        verify(auditoriaService).registrar(
                eq("ATIVO"),
                eq(response.getId().toString()),
                eq("CRIACAO"),
                eq("Ativo cadastrado: Notebook Dell / patrimonio PAT-001 / status ESTOQUE"));
    }

    @Test
    void criarAtivoDeveBloquearPatrimonioDuplicado() {
        AtivoRequestDTO request = new AtivoRequestDTO();
        request.setPatrimonio("pat-001");

        when(ativoRepository.existsByPatrimonioIgnoreCase("PAT-001")).thenReturn(true);

        assertThatThrownBy(() -> ativoService.criarAtivo(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe um ativo com o patrimonio informado");

        verify(ativoRepository, never()).save(any(Ativo.class));
        verify(auditoriaService, never()).registrar(any(), any(), any(), any());
    }
}
