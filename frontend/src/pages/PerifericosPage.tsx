import { useEffect, useState } from 'react'
import { X } from 'lucide-react'
import { API, type Periferico, type TipoPeriferico } from '../services/api'

const EMPTY_FORM: Periferico = {
  nome: '',
  tipo: 'KIT_MOUSE_TECLADO',
  quantidade: 0,
  observacoes: '',
}

export function PerifericosPage() {
  const [items, setItems] = useState<Periferico[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [totalElements, setTotalElements] = useState(0)
  const [form, setForm] = useState<Periferico>(EMPTY_FORM)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)

  async function loadData(customPage = page) {
    try {
      const data = await API.getPerifericos({ page: customPage, size: 20, sort: 'updatedAt,desc' })
      setItems(data.content)
      setPage(data.page)
      setTotalPages(Math.max(1, data.totalPages))
      setTotalElements(data.totalElements)
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Erro ao carregar perifericos')
    }
  }

  useEffect(() => {
    void loadData(0)
  }, [])

  useEffect(() => {
    if (!modalOpen) return

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [modalOpen])

  function resetForm() {
    setForm(EMPTY_FORM)
    setEditingId(null)
    setModalOpen(false)
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setSaving(true)
    try {
      if (editingId) {
        await API.updatePeriferico(editingId, form)
      } else {
        await API.createPeriferico(form)
      }
      resetForm()
      await loadData(0)
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Erro ao salvar periferico')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: string) {
    if (!window.confirm('Deseja excluir este perifetrico?')) return
    try {
      await API.deletePeriferico(id)
      await loadData(items.length === 1 && page > 0 ? page - 1 : page)
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Erro ao excluir periferico')
    }
  }

  function startEdit(item: Periferico) {
    setEditingId(item.id ?? null)
    setForm(item)
    setModalOpen(true)
  }

  return (
    <div className="page-stack">
      <header className="page-title-row">
        <div>
          <span className="eyebrow">Perifericos</span>
          <h2>Perifericos</h2>
          
        </div>
        <div className="header-actions">
          <button
            className="primary-button"
            type="button"
            onClick={() => {
              setForm(EMPTY_FORM)
              setEditingId(null)
              setModalOpen(true)
            }}
          >
            Novo periferico
          </button>
        </div>
      </header>

      <section className="panel">
        <div className="panel-head">
          <h3>Estoque de perifericos</h3>
          <span>{totalElements} registro(s)</span>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Tipo</th>
                <th>Quantidade</th>
                <th>Observacoes</th>
                <th>Acoes</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id}>
                  <td data-label="Nome">{item.nome}</td>
                  <td data-label="Tipo">{item.tipo}</td>
                  <td data-label="Quantidade">{item.quantidade}</td>
                  <td data-label="Observacoes">{item.observacoes || '-'}</td>
                  <td className="actions-cell">
                    <button type="button" className="table-link" onClick={() => startEdit(item)}>Editar</button>
                    {item.id && (
                      <button type="button" className="danger-link" onClick={() => void handleDelete(item.id!)}>
                        Excluir
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {items.length === 0 && (
                <tr>
                  <td colSpan={5} className="empty-copy">Nenhum periferico registrado.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section className="panel">
        <div className="header-actions">
          <button className="ghost-button" type="button" disabled={page === 0} onClick={() => void loadData(page - 1)}>
            Pagina anterior
          </button>
          <span className="empty-copy">Pagina {page + 1} de {totalPages}</span>
          <button className="ghost-button" type="button" disabled={page + 1 >= totalPages} onClick={() => void loadData(page + 1)}>
            Proxima pagina
          </button>
        </div>
      </section>

      {modalOpen && (
        <div className="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="periferico-modal-title">
          <div className="modal-shell">
            <button
              type="button"
              className="modal-close-button"
              aria-label="Fechar cadastro de periferico"
              onClick={resetForm}
            >
              <X size={20} strokeWidth={2.2} />
            </button>

            <form className="panel form-panel" onSubmit={handleSubmit}>
              <div className="page-title-row modal-title-row">
                <div>
                  <h2 id="periferico-modal-title">{editingId ? 'Editar periferico' : 'Cadastrar periferico'}</h2>
                  <p>Modulo auxiliar para contagem de itens sem patrimonio.</p>
                </div>
              </div>

              <div className="form-grid">
                <label>
                  Nome
                  <input
                    value={form.nome}
                    onChange={(event) => setForm((current) => ({ ...current, nome: event.target.value }))}
                    required
                  />
                </label>
                <label>
                  Categoria
                  <select
                    value={form.tipo}
                    onChange={(event) => setForm((current) => ({ ...current, tipo: event.target.value as TipoPeriferico }))}
                  >
                    <option value="KIT_MOUSE_TECLADO">Kit mouse e teclado</option>
                    <option value="ADAPTADOR">Adaptador</option>
                    <option value="FONE">Fone</option>
                    <option value="OUTRO">Outro</option>
                  </select>
                </label>
                <label>
                  Quantidade
                  <input
                    type="number"
                    min={0}
                    value={form.quantidade}
                    onChange={(event) => setForm((current) => ({ ...current, quantidade: Number(event.target.value) }))}
                    required
                  />
                </label>
                <label className="full-width">
                  OBS
                  <textarea
                    rows={4}
                    value={form.observacoes ?? ''}
                    onChange={(event) => setForm((current) => ({ ...current, observacoes: event.target.value }))}
                  />
                </label>
              </div>

              <div className="header-actions modal-actions">
                <button type="button" className="ghost-button" onClick={resetForm}>
                  Cancelar
                </button>
                <button className="primary-button" type="submit" disabled={saving}>
                  {saving ? 'Salvando...' : editingId ? 'Atualizar' : 'Criar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
