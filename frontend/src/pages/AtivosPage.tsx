import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { X } from 'lucide-react'
import { AtivoForm } from '../components/AtivoForm'
import { API, type Ativo } from '../services/api'

const STATUS_LABEL: Record<string, string> = {
  OPERACIONAL: 'Operacional',
  ESTOQUE: 'Estoque',
  MANUTENCAO: 'Manutencao',
}

export function AtivosPage() {
  const [ativos, setAtivos] = useState<Ativo[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [termo, setTermo] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)

  async function loadData(customTermo = termo, customPage = page) {
    setLoading(true)
    try {
      const data = await API.getAtivos({ termo: customTermo, page: customPage, size: 20, sort: 'updatedAt,desc' })
      setAtivos(data.content)
      setPage(data.page)
      setTotalPages(Math.max(1, data.totalPages))
      setTotalElements(data.totalElements)
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Erro ao listar ativos')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadData('', 0)
  }, [])

  useEffect(() => {
    if (!modalOpen) return

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [modalOpen])

  async function handleDelete(id: string) {
    if (!window.confirm('Deseja excluir este ativo?')) return
    try {
      await API.deleteAtivo(id)
      await loadData(termo, ativos.length === 1 && page > 0 ? page - 1 : page)
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Erro ao excluir ativo')
    }
  }

  async function handleCreateAtivo(form: Ativo) {
    setSaving(true)
    try {
      await API.createAtivo(form)
      setModalOpen(false)
      await loadData(termo, 0)
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Erro ao salvar ativo')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="page-stack">
      <header className="page-title-row">
        <div>
          <span className="eyebrow">Ativos</span>
          <h2>Ativos</h2>
          <p>Busca unica por nome, responsavel ou patrimonio. Todas as regras sao validadas no backend.</p>
        </div>
        <div className="header-actions">
          <button className="secondary-button" type="button" onClick={() => void API.exportTxt({ termo })}>
            Exportar TXT
          </button>
          <button className="primary-button" type="button" onClick={() => setModalOpen(true)}>
            Novo ativo
          </button>
        </div>
      </header>

      <section className="panel">
        <div className="search-single-field">
          <label>
            Buscar
            <input
              value={termo}
              onChange={(event) => setTermo(event.target.value)}
              placeholder="Nome, responsavel ou patrimonio"
            />
          </label>
        </div>
        <div className="header-actions">
          <button className="primary-button" type="button" onClick={() => void loadData(termo, 0)}>
            Filtrar
          </button>
          <button
            className="ghost-button"
            type="button"
            onClick={() => {
              setTermo('')
              void loadData('', 0)
            }}
          >
            Limpar
          </button>
        </div>
      </section>

      <section className="panel">
        <div className="panel-head">
          <h3>Ativos cadastrados</h3>
          <span>{totalElements} registro(s)</span>
        </div>

        {loading ? (
          <p className="empty-copy">Carregando ativos...</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>Patrimonio</th>
                  <th>Status</th>
                  <th>Responsavel</th>
                  <th>Setor</th>
                  <th>Categoria</th>
                  <th>MAC Ethernet</th>
                  <th>Acoes</th>
                </tr>
              </thead>
              <tbody>
                {ativos.map((ativo) => (
                  <tr key={ativo.id}>
                    <td data-label="Nome">{ativo.nomeAtivo}</td>
                    <td data-label="Patrimonio">{ativo.patrimonio}</td>
                    <td data-label="Status">{STATUS_LABEL[ativo.status] ?? ativo.status}</td>
                    <td data-label="Responsavel">{ativo.responsavel}</td>
                    <td data-label="Setor">{ativo.setor}</td>
                    <td data-label="Categoria">{ativo.categoria}</td>
                    <td data-label="MAC Ethernet">{ativo.macAddressEthernet}</td>
                    <td className="actions-cell">
                      <Link to={`/ativos/${ativo.id}`} className="table-link">Ver</Link>
                      <Link to={`/ativos/${ativo.id}/editar`} className="table-link">Editar</Link>
                      {ativo.id && (
                        <button className="danger-link" type="button" onClick={() => void handleDelete(ativo.id!)}>
                          Excluir
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
                {ativos.length === 0 && (
                  <tr>
                    <td colSpan={8} className="empty-copy">Nenhum ativo encontrado.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel">
        <div className="header-actions">
          <button className="ghost-button" type="button" disabled={page === 0} onClick={() => void loadData(termo, page - 1)}>
            Pagina anterior
          </button>
          <span className="empty-copy">Pagina {page + 1} de {totalPages}</span>
          <button className="ghost-button" type="button" disabled={page + 1 >= totalPages} onClick={() => void loadData(termo, page + 1)}>
            Proxima pagina
          </button>
        </div>
      </section>

      {modalOpen && (
        <div className="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="ativo-modal-title">
          <div className="modal-shell">
            <button
              type="button"
              className="modal-close-button"
              aria-label="Fechar cadastro de ativo"
              onClick={() => setModalOpen(false)}
            >
              <X size={20} strokeWidth={2.2} />
            </button>

            <AtivoForm
              saving={saving}
              heading="Cadastrar ativo"
              description="Formulario aderente ao PRD, sem campos legados."
              submitLabel="Criar ativo"
              savingLabel="Criando..."
              onCancel={() => setModalOpen(false)}
              onSubmit={handleCreateAtivo}
            />
          </div>
        </div>
      )}
    </div>
  )
}
