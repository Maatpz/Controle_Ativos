import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { AtivoForm } from '../components/AtivoForm'
import { API, type Ativo } from '../services/api'

export function AtivoFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const editing = Boolean(id)
  const [form, setForm] = useState<Ativo>()
  const [loading, setLoading] = useState(editing)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!editing || !id) return
    API.getAtivoById(id)
      .then((data) => setForm(data))
      .catch((error) => alert(error instanceof Error ? error.message : 'Erro ao carregar ativo'))
      .finally(() => setLoading(false))
  }, [editing, id])

  async function handleSubmit(data: Ativo) {
    setSaving(true)
    try {
      if (editing && id) {
        await API.updateAtivo(id, data)
      } else {
        await API.createAtivo(data)
      }
      navigate('/ativos')
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
          <h2>{editing ? 'Editar ativo' : 'Novo ativo'}</h2>
        </div>
      </header>

      <AtivoForm
        initialData={form}
        loading={loading}
        saving={saving}
        heading={editing ? 'Editar ativo' : 'Novo ativo'}
        submitLabel="Salvar ativo"
        savingLabel="Salvando..."
        onCancel={() => navigate('/ativos')}
        onSubmit={handleSubmit}
      />
    </div>
  )
}
