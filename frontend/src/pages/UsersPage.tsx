import { useEffect, useState } from 'react'
import { X } from 'lucide-react'
import { API, type Role, type Usuario } from '../services/api'

type UserForm = {
  username: string
  nome: string
  password: string
  role: Role
  ativo: boolean
}

const EMPTY_FORM: UserForm = {
  username: '',
  nome: '',
  password: '',
  role: 'USER',
  ativo: true,
}

export function UsersPage() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [resumo, setResumo] = useState({ totalUsuarios: 0, admins: 0, users: 0 })
  const [form, setForm] = useState<UserForm>(EMPTY_FORM)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)

  async function loadData(customPage = page) {
    try {
      const [list, summary] = await Promise.all([
        API.getUsuarios({ page: customPage, size: 20, sort: 'createdAt,desc' }),
        API.getUsuariosResumo(),
      ])
      setUsuarios(list.content)
      setPage(list.page)
      setTotalPages(Math.max(1, list.totalPages))
      setResumo(summary)
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Erro ao carregar usuarios')
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
        await API.updateUsuario(editingId, {
          username: form.username,
          nome: form.nome,
          password: form.password || undefined,
          role: form.role,
          ativo: form.ativo,
        })
      } else {
        await API.createUsuario(form)
      }
      resetForm()
      await loadData(page)
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Erro ao salvar usuario')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(id: string) {
    if (!window.confirm('Deseja excluir este usuario?')) return
    try {
      await API.deleteUsuario(id)
      await loadData(usuarios.length === 1 && page > 0 ? page - 1 : page)
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Erro ao excluir usuario')
    }
  }

  function startEdit(usuario: Usuario) {
    setEditingId(usuario.id)
    setForm({
      username: usuario.username,
      nome: usuario.nome,
      password: '',
      role: usuario.role,
      ativo: usuario.ativo,
    })
    setModalOpen(true)
  }

  return (
    <div className="page-stack">
      <header className="page-title-row">
        <div>
          <span className="eyebrow">Usuarios</span>
          <h2>Usuarios</h2>
          <p>Somente perfil ADMIN consegue cadastrar, alterar e excluir usuarios.</p>
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
            Novo usuario
          </button>
        </div>
      </header>

      <section className="stats-grid">
        <article className="stat-card accent">
          <span>Total de usuarios</span>
          <strong>{resumo.totalUsuarios}</strong>
        </article>
        <article className="stat-card accent">
          <span>Admins</span>
          <strong>{resumo.admins}</strong>
        </article>
        <article className="stat-card accent">
          <span>Users</span>
          <strong>{resumo.users}</strong>
        </article>
      </section>

      <section className="panel">
        <div className="panel-head">
          <h3>Usuarios cadastrados</h3>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Username</th>
                <th>Nome</th>
                <th>Perfil</th>
                <th>Status</th>
                <th>Criado em</th>
                <th>Acoes</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map((usuario) => (
                <tr key={usuario.id}>
                  <td data-label="Username">{usuario.username}</td>
                  <td data-label="Nome">{usuario.nome}</td>
                  <td data-label="Perfil">{usuario.role}</td>
                  <td data-label="Status">{usuario.ativo ? 'Ativo' : 'Inativo'}</td>
                  <td data-label="Criado em">{usuario.createdAt ? new Date(usuario.createdAt).toLocaleString('pt-BR') : '-'}</td>
                  <td className="actions-cell">
                    <button type="button" className="table-link" onClick={() => startEdit(usuario)}>Editar</button>
                    <button type="button" className="danger-link" onClick={() => void handleDelete(usuario.id)}>Excluir</button>
                  </td>
                </tr>
              ))}
              {usuarios.length === 0 && (
                <tr>
                  <td colSpan={6} className="empty-copy">Nenhum usuario cadastrado.</td>
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
        <div className="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="usuario-modal-title">
          <div className="modal-shell modal-shell-compact">
            <button
              type="button"
              className="modal-close-button"
              aria-label="Fechar cadastro de usuario"
              onClick={resetForm}
            >
              <X size={20} strokeWidth={2.2} />
            </button>

            <form className="panel form-panel" onSubmit={handleSubmit}>
              <div className="page-title-row modal-title-row">
                <div>
                  <h2 id="usuario-modal-title">{editingId ? 'Editar usuario' : 'Criar usuario'}</h2>
                  <p>Controle administrativo de acesso e perfil.</p>
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
                  Username
                  <input
                    value={form.username}
                    onChange={(event) => setForm((current) => ({ ...current, username: event.target.value }))}
                    required
                  />
                </label>
                <label>
                  Senha
                  <input
                    type="password"
                    value={form.password}
                    onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
                    required={!editingId}
                  />
                </label>
                <label>
                  Perfil
                  <select
                    value={form.role}
                    onChange={(event) => setForm((current) => ({ ...current, role: event.target.value as Role }))}
                  >
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </label>
                <label>
                  Status
                  <select
                    value={form.ativo ? 'true' : 'false'}
                    onChange={(event) => setForm((current) => ({ ...current, ativo: event.target.value === 'true' }))}
                  >
                    <option value="true">ATIVO</option>
                    <option value="false">INATIVO</option>
                  </select>
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
