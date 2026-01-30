package com.matheus.controle.ativos.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.matheus.controle.ativos.model.Ativo;
import com.matheus.controle.ativos.model.enums.Status;
import com.matheus.controle.ativos.repository.AtivoRepository;

@Service
public class AtivoService {

    @Autowired
    private AtivoRepository ativoRepository;

    public List<Ativo> findAll() {
        return ativoRepository.findAll();
    }

    public Optional<Ativo> findById(UUID id) {
        return ativoRepository.findById(id);
    }

    public Ativo save(Ativo ativo) {
        return ativoRepository.save(ativo);
    }

    public void deleteById(UUID id) {
        ativoRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return ativoRepository.existsById(id);
    }

    public List<Ativo> findByNome(String nomeAtivo) {
        return ativoRepository.findByNomeAtivoContainingIgnoreCase(nomeAtivo);
    }

    public List<Ativo> findByResponsavel(String responsavel) {
        return ativoRepository.findByResponsavelContainingIgnoreCase(responsavel);
    }

    public Optional<Ativo> findByPatrimonio(String patrimonio) {
        return ativoRepository.finByPatrimonio(patrimonio);
    }

    public List<Ativo> findByStatus(Status status) {
        return ativoRepository.findByStatus(status);
    }

    // public List<Ativo> findByCategoria(String categoria) {
    // return ativoRepository.findByCategoriaContainingIgnoreCase(categoria);
    // }

    public List<Ativo> findBySetor(String setor) {
        return ativoRepository.findBySetorContainingIgnoreCase(setor);
    }

    public List<Ativo> findByLocalidade(String localidade) {
        return ativoRepository.findByLocalidadeContainingIgnoreCase(localidade);
    }

    // em fase
    public List<Ativo> findByMultipleFields(String nome, String responsavel,
            String patrimonio, String setor, Status status) {
        return ativoRepository.findByMultipleFields(nome, responsavel, patrimonio,
                setor, status);
    }

    // Busca geral por termo em fase
    public List<Ativo> findByTermoGeral(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return findAll();
        }
        return ativoRepository.findByTermoGeral(termo.trim());
    }

    public Ativo updateAtivo(UUID id, Ativo ativoAtualizado) {
        Optional<Ativo> ativoExistente = findById(id);
        if (ativoExistente.isPresent()) {
            Ativo ativo = ativoExistente.get();

            if (ativoAtualizado.getNomeAtivo() != null)
                ativo.setNomeAtivo(ativoAtualizado.getNomeAtivo());
            // if (ativoAtualizado.getDominio() != null)
            // ativo.setDominio(ativoAtualizado.getDominio());
            if (ativoAtualizado.getStatus() != null)
                ativo.setStatus(ativoAtualizado.getStatus());
            if (ativoAtualizado.getLocalidade() != null)
                ativo.setLocalidade(ativoAtualizado.getLocalidade());
            if (ativoAtualizado.getSetor() != null)
                ativo.setSetor(ativoAtualizado.getSetor());
            if (ativoAtualizado.getResponsavel() != null)
                ativo.setResponsavel(ativoAtualizado.getResponsavel());
            // if (ativoAtualizado.getLicenca() != null)
            // ativo.setLicenca(ativoAtualizado.getLicenca());
            // if (ativoAtualizado.getCategoria() != null)
            // ativo.setCategoria(ativoAtualizado.getCategoria());
            // if (ativoAtualizado.getMarcaFabricante() != null)
            // ativo.setMarcaFabricante(ativoAtualizado.getMarcaFabricante());
            // if (ativoAtualizado.getModelo() != null)
            // ativo.setModelo(ativoAtualizado.getModelo());
            // if (ativoAtualizado.getSerialNumber() != null)
            // ativo.setSerialNumber(ativoAtualizado.getSerialNumber());
            if (ativoAtualizado.getPatrimonio() != null)
                ativo.setPatrimonio(ativoAtualizado.getPatrimonio());
            // if (ativoAtualizado.getLinha() != null)
            // ativo.setLinha(ativoAtualizado.getLinha());
            // if (ativoAtualizado.getImei() != null)
            // ativo.setImei(ativoAtualizado.getImei());
            // if (ativoAtualizado.getMacEthernet() != null)
            // ativo.setMacEthernet(ativoAtualizado.getMacEthernet());
            // if (ativoAtualizado.getMacWifi() != null)
            // ativo.setMacWifi(ativoAtualizado.getMacWifi());
            // if (ativoAtualizado.getIpFixado() != null)
            // ativo.setIpFixado(ativoAtualizado.getIpFixado());
            // if (ativoAtualizado.getProcessador() != null)
            // ativo.setProcessador(ativoAtualizado.getProcessador());
            // if (ativoAtualizado.getMemoriaRam() != null)
            // ativo.setMemoriaRam(ativoAtualizado.getMemoriaRam());
            // if (ativoAtualizado.getArmazenamento() != null)
            // ativo.setArmazenamento(ativoAtualizado.getArmazenamento());
            // if (ativoAtualizado.getCameraIntegrada() != null)
            // ativo.setCameraIntegrada(ativoAtualizado.getCameraIntegrada());
            // if (ativoAtualizado.getClassificacaoInfo() != null)
            // ativo.setClassificacaoInfo(ativoAtualizado.getClassificacaoInfo());
            // if (ativoAtualizado.getConfidencialidade() != null)
            // ativo.setConfidencialidade(ativoAtualizado.getConfidencialidade());
            // if (ativoAtualizado.getIntegridade() != null)
            // ativo.setIntegridade(ativoAtualizado.getIntegridade());
            // if (ativoAtualizado.getDisponibilidade() != null)
            // ativo.setDisponibilidade(ativoAtualizado.getDisponibilidade());
            // if (ativoAtualizado.getFornecedor() != null)
            // ativo.setFornecedor(ativoAtualizado.getFornecedor());
            // if (ativoAtualizado.getDataAquisicao() != null)
            // ativo.setDataAquisicao(ativoAtualizado.getDataAquisicao());
            // if (ativoAtualizado.getGarantiaSuporte() != null)
            // ativo.setGarantiaSuporte(ativoAtualizado.getGarantiaSuporte());
            // if (ativoAtualizado.getContatoSuporte() != null)
            // ativo.setContatoSuporte(ativoAtualizado.getContatoSuporte());
            // if (ativoAtualizado.getNotaFiscal() != null)
            // ativo.setNotaFiscal(ativoAtualizado.getNotaFiscal());
            // if (ativoAtualizado.getRegistroMudanca() != null)
            // ativo.setRegistroMudanca(ativoAtualizado.getRegistroMudanca());
            // if (ativoAtualizado.getDataUltimaAvaliacao() != null)
            // ativo.setDataUltimaAvaliacao(ativoAtualizado.getDataUltimaAvaliacao());
            // if (ativoAtualizado.getProximaAvaliacao() != null)
            // ativo.setProximaAvaliacao(ativoAtualizado.getProximaAvaliacao());
            // if (ativoAtualizado.getBitlocker() != null)
            // ativo.setBitlocker(ativoAtualizado.getBitlocker());
            // if (ativoAtualizado.getAntivirusLicenca() != null)
            // ativo.setAntivirusLicenca(ativoAtualizado.getAntivirusLicenca());
            // if (ativoAtualizado.getAntivirusVersao() != null)
            // ativo.setAntivirusVersao(ativoAtualizado.getAntivirusVersao());
            // if (ativoAtualizado.getBartWazuh() != null)
            // ativo.setBartWazuh(ativoAtualizado.getBartWazuh());
            // if (ativoAtualizado.getChromeEnterprise() != null)
            // ativo.setChromeEnterprise(ativoAtualizado.getChromeEnterprise());
            // if (ativoAtualizado.getAcessoRemotoId() != null)
            // ativo.setAcessoRemotoId(ativoAtualizado.getAcessoRemotoId());
            // if (ativoAtualizado.getAcessoRemotoSenha() != null)
            // ativo.setAcessoRemotoSenha(ativoAtualizado.getAcessoRemotoSenha());
            // if (ativoAtualizado.getTermoCustodia() != null)
            // ativo.setTermoCustodia(ativoAtualizado.getTermoCustodia());
            // if (ativoAtualizado.getSenhaAdmin() != null)
            // ativo.setSenhaAdmin(ativoAtualizado.getSenhaAdmin());
            // if (ativoAtualizado.getBloqueioTela() != null)
            // ativo.setBloqueioTela(ativoAtualizado.getBloqueioTela());
            // if (ativoAtualizado.getContaAtribuida() != null)
            // ativo.setContaAtribuida(ativoAtualizado.getContaAtribuida());
            // if (ativoAtualizado.getSenhaContaAtribuida() != null)
            // ativo.setSenhaContaAtribuida(ativoAtualizado.getSenhaContaAtribuida());
            // if (ativoAtualizado.getContratoProcedimentos() != null)
            // ativo.setContratoProcedimentos(ativoAtualizado.getContratoProcedimentos());
            // if (ativoAtualizado.getItensConfiguracao() != null)
            // ativo.setItensConfiguracao(ativoAtualizado.getItensConfiguracao());
            // if (ativoAtualizado.getManutencaoPreventiva() != null)
            // ativo.setManutencaoPreventiva(ativoAtualizado.getManutencaoPreventiva());
            // if (ativoAtualizado.getObs() != null) ativo.setObs(ativoAtualizado.getObs());

            return save(ativo);
        }
        return null;

    }

}
