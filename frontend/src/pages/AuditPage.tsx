import { useEffect, useState } from 'react'
import { API, type AuditoriaLog } from '../services/api'

export function AuditPage() {
  const [logs, setLogs] = useState<AuditoriaLog[]>([])
  const [usuarioFiltro, setUsuarioFiltro] = useState('')
  const [dataFiltro, setDataFiltro] = useState('')
  const [acaoFiltro, setAcaoFiltro] = useState('')
  const [filteredLogs, setFilteredLogs] = useState<AuditoriaLog[]>([])

  async function loadLogs() {
    try {
      const data = await API.getAuditorias({ page: 0, size: 100 })
      setLogs(data.content)
      setFilteredLogs(data.content)
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Erro ao carregar auditoria')
    }
  }

  useEffect(() => {
    void loadLogs()
  }, [])

  useEffect(() => {
    const timer = window.setInterval(() => {
      void loadLogs()
    }, 5000)
    return () => window.clearInterval(timer)
  }, [])

  function isSuspeita(log: AuditoriaLog) {
    const base = `${log.acao} ${log.detalhes}`.toLowerCase()
    return base.includes('exclu') || base.includes('delete') || base.includes('falh') || base.includes('negad')
  }

  function handleApplyFilters() {
    const nextLogs = logs.filter((log) => {
      const matchesUsuario = !usuarioFiltro || log.usuario.toLowerCase().includes(usuarioFiltro.toLowerCase())
      const matchesAcao = !acaoFiltro || log.acao === acaoFiltro
      const matchesData = !dataFiltro || new Date(log.createdAt).toISOString().slice(0, 10) === dataFiltro

      return matchesUsuario && matchesAcao && matchesData
    })

    setFilteredLogs(nextLogs)
  }

  function handleExportLogs() {
    const rows = [
      ['Data', 'Usuario', 'Perfil', 'Acao', 'Recurso', 'Suspeita', 'Descricao'],
      ...filteredLogs.map((log) => [
        new Date(log.createdAt).toLocaleString('pt-BR'),
        log.usuario,
        log.perfilUsuario,
        log.acao,
        log.entidade,
        isSuspeita(log) ? 'Sim' : 'Nao',
        log.detalhes.replace(/[\r\n]+/g, ' '),
      ]),
    ]

    const csv = rows
      .map((row) => row.map((value) => `"${String(value).replaceAll('"', '""')}"`).join(';'))
      .join('\n')

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'auditoria-logs.csv'
    link.click()
    window.URL.revokeObjectURL(url)
  }

  const actionOptions = Array.from(new Set(logs.map((log) => log.acao))).sort()

  return (
    <div className="page-stack">
      <section className="audit-filter-panel">
        <div className="audit-title-block">
          <h2>Auditoria e rastreabilidade</h2>
          <p>Log de quem fez, quando fez e o que foi alterado. Visivel apenas para administradores.</p>
        </div>
        <div className="audit-filter-row">
          <input
            value={usuarioFiltro}
            onChange={(event) => setUsuarioFiltro(event.target.value)}
            placeholder="Filtrar por usuario"
          />
          <input
            type="date"
            value={dataFiltro}
            onChange={(event) => setDataFiltro(event.target.value)}
          />
          <select value={acaoFiltro} onChange={(event) => setAcaoFiltro(event.target.value)}>
            <option value="">Tipo de acao</option>
            {actionOptions.map((acao) => (
              <option key={acao} value={acao}>
                {acao}
              </option>
            ))}
          </select>
          <button type="button" className="audit-apply-button" onClick={handleApplyFilters}>
            Aplicar
          </button>
          <button type="button" className="audit-export-button" onClick={handleExportLogs}>
            Exportar logs
          </button>
        </div>
      </section>

      <section className="audit-table-panel">
        <div className="table-wrap">
          <table className="audit-table">
            <thead>
              <tr>
                <th>Acao</th>
                <th>Data</th>
                <th>Usuario</th>
                <th>Perfil</th>
                <th>Recurso</th>
                <th>Suspeita</th>
                <th>Descricao</th>
              </tr>
            </thead>
            <tbody>
              {filteredLogs.map((log) => (
                <tr key={log.id}>
                  <td data-label="Acao">{log.acao}</td>
                  <td data-label="Data">{new Date(log.createdAt).toLocaleString('pt-BR')}</td>
                  <td data-label="Usuario">{log.usuario}</td>
                  <td data-label="Perfil">{log.perfilUsuario}</td>
                  <td data-label="Recurso">{log.entidade}</td>
                  <td data-label="Suspeita">{isSuspeita(log) ? 'Sim' : 'Nao'}</td>
                  <td data-label="Descricao">{log.detalhes}</td>
                </tr>
              ))}
              {filteredLogs.length === 0 && (
                <tr>
                  <td colSpan={7} className="empty-copy">Nenhum evento de auditoria registrado.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  )
}
