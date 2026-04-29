import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { API, type Ativo } from '../services/api'

const STATUS_LABEL: Record<string, string> = {
  OPERACIONAL: 'Operacional',
  ESTOQUE: 'Estoque',
  MANUTENCAO: 'Manutencao',
}

export function AtivoDetailsPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [ativo, setAtivo] = useState<Ativo | null>(null)

  useEffect(() => {
    if (!id) return
    API.getAtivoById(id)
      .then(setAtivo)
      .catch((error) => {
        alert(error instanceof Error ? error.message : 'Erro ao carregar ativo')
        navigate('/ativos')
      })
  }, [id, navigate])

  if (!ativo) {
    return <div className="page-loader">Carregando ativo...</div>
  }

  return (
    <div className="page-stack">
      <header className="page-title-row">
        <div>
          <span className="eyebrow">Ativo</span>
          <h2>{ativo.nomeAtivo}</h2>
          <p>{STATUS_LABEL[ativo.status] ?? ativo.status}</p>
        </div>
      </header>

      <section className="panel detail-grid">
        <div><span>Patrimonio</span><strong>{ativo.patrimonio}</strong></div>
        <div><span>Setor</span><strong>{ativo.setor}</strong></div>
        <div><span>Responsavel</span><strong>{ativo.responsavel}</strong></div>
        <div><span>Categoria</span><strong>{ativo.categoria}</strong></div>
        <div><span>MAC Ethernet</span><strong>{ativo.macAddressEthernet}</strong></div>
        <div><span>Atualizado em</span><strong>{ativo.updatedAt ? new Date(ativo.updatedAt).toLocaleString('pt-BR') : '-'}</strong></div>
        <div className="full-width"><span>Observacoes</span><p>{ativo.observacoes}</p></div>
      </section>

      <div className="header-actions">
        <Link className="ghost-button" to="/ativos">Voltar</Link>
        <Link className="primary-button" to={`/ativos/${ativo.id}/editar`}>Editar</Link>
      </div>
    </div>
  )
}
