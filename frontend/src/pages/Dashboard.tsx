import { useEffect, useState } from 'react'
import { ChartCard } from '../components/ChartCard'
import { useAuth } from '../hooks/useAuth'
import { API, type AuditoriaLog, type DashboardData } from '../services/api'

export function Dashboard() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'
  const [dashboard, setDashboard] = useState<DashboardData | null>(null)
  const [auditorias, setAuditorias] = useState<AuditoriaLog[]>([])
  const [auditPage, setAuditPage] = useState(0)
  const [auditTotalPages, setAuditTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)

  async function loadDashboard() {
    const data = await API.getDashboard()
    setDashboard(data)
  }

  async function loadAudits(customPage = auditPage) {
    if (!isAdmin) return
    const data = await API.getAuditorias({ page: customPage, size: 10 })
    setAuditorias(data.content)
    setAuditTotalPages(Math.max(1, data.totalPages))
  }

  useEffect(() => {
    Promise.all([loadDashboard(), loadAudits(0)])
      .catch((error: unknown) => {
        alert(error instanceof Error ? error.message : 'Erro ao carregar dashboard')
      })
      .finally(() => setLoading(false))
  }, [isAdmin])

  useEffect(() => {
    const refreshDashboard = window.setInterval(() => {
      void loadDashboard()
    }, 30000)

    return () => window.clearInterval(refreshDashboard)
  }, [])

  useEffect(() => {
    if (!isAdmin) return
    const refreshAudits = window.setInterval(() => {
      void loadAudits(auditPage)
    }, 5000)

    return () => window.clearInterval(refreshAudits)
  }, [auditPage, isAdmin])

  if (loading || !dashboard) {
    return <div className="page-loader">Carregando dashboard...</div>
  }

  const ativos = dashboard.ativos
  const perifericosPorCategoria = dashboard.perifericos.porTipo

  return (
    <div className="page-stack">
      <header className="page-title-row">
        <div>
          <span className="eyebrow">Dashboard</span>
          <h2>Dashboard</h2>
          <p>Resumo de ativos</p>
        </div>
      </header>

      <section className="stats-grid">
        <article className="stat-card stat-card-total">
          <span>Total de ativos</span>
          <strong>{ativos.totalAtivos}</strong>
        </article>
        <article className="stat-card stat-card-operacional">
          <span>Operacionais</span>
          <strong>{ativos.operacionais}</strong>
        </article>
        <article className="stat-card stat-card-estoque">
          <span>Em estoque</span>
          <strong>{ativos.estoque}</strong>
        </article>
        <article className="stat-card stat-card-manutencao">
          <span>Manutencao</span>
          <strong>{ativos.manutencao}</strong>
        </article>
      </section>

      <section className="charts-grid">
        <ChartCard title="Ativos por setor" data={ativos.porSetor} type="pie" />
        <ChartCard title="Ativos por categoria" data={ativos.porCategoria} type="bar" />
        <ChartCard title="Perifericos por categoria" data={perifericosPorCategoria} type="pie" />
      </section>

      {isAdmin && (
        <section className="panel">
          <div className="panel-head">
            <div>
              <h3>Atividades Recentes</h3>
            </div>
            {auditTotalPages > 1 && (
              <div className="audit-page-tabs">
                {Array.from({ length: auditTotalPages }, (_, index) => (
                  <button
                    key={index}
                    type="button"
                    className={index === auditPage ? 'audit-page-tab active' : 'audit-page-tab'}
                    onClick={() => {
                      setAuditPage(index)
                      void loadAudits(index)
                    }}
                  >
                    Aba {index + 1}
                  </button>
                ))}
              </div>
            )}
          </div>
          <div className="audit-feed">
            {auditorias.map((item) => (
              <article key={item.id} className="audit-card">
                <div className="audit-meta">
                  <strong>{item.acao}</strong>
                  <span>{new Date(item.createdAt).toLocaleString('pt-BR')}</span>
                </div>
                <p>{item.detalhes}</p>
                <small>{item.usuario} | {item.perfilUsuario} | {item.entidade}</small>
              </article>
            ))}
            {auditorias.length === 0 && <p className="empty-copy">Sem eventos ate o momento.</p>}
          </div>
        </section>
      )}
    </div>
  )
}
