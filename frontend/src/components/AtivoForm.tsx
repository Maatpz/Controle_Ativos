import { useEffect, useState } from 'react'
import type { Ativo, Status } from '../services/api'

const EMPTY_FORM: Ativo = {
  nomeAtivo: '',
  setor: '',
  responsavel: '',
  categoria: '',
  patrimonio: '',
  status: 'OPERACIONAL',
  macAddressEthernet: '',
  observacoes: '',
}

type AtivoFormProps = {
  initialData?: Ativo
  loading?: boolean
  saving?: boolean
  heading: string
  description?: string
  submitLabel: string
  savingLabel: string
  onCancel: () => void
  onSubmit: (form: Ativo) => Promise<void> | void
}

export function AtivoForm({
  initialData,
  loading = false,
  saving = false,
  heading,
  description,
  submitLabel,
  savingLabel,
  onCancel,
  onSubmit,
}: AtivoFormProps) {
  const [form, setForm] = useState<Ativo>(initialData ?? EMPTY_FORM)

  useEffect(() => {
    setForm(initialData ?? EMPTY_FORM)
  }, [initialData])

  function updateField<K extends keyof Ativo>(field: K, value: Ativo[K]) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    await onSubmit(form)
  }

  if (loading) {
    return <div className="page-loader">Carregando ativo...</div>
  }

  return (
    <form className="panel form-panel" onSubmit={handleSubmit}>
      <div className="page-title-row modal-title-row">
        <div>
          <h2>{heading}</h2>
          {description && <p>{description}</p>}
        </div>
      </div>

      <div className="form-grid">
        <label>
          Nome do ativo
          <input value={form.nomeAtivo} onChange={(event) => updateField('nomeAtivo', event.target.value)} />
        </label>
        <label>
          Patrimonio
          <input value={form.patrimonio} onChange={(event) => updateField('patrimonio', event.target.value)} />
        </label>
        <label>
          Status
          <select value={form.status} onChange={(event) => updateField('status', event.target.value as Status)}>
            <option value="OPERACIONAL">Operacional</option>
            <option value="ESTOQUE">Estoque</option>
            <option value="MANUTENCAO">Manutencao</option>
          </select>
        </label>
        <label>
          Endereco MAC
          <input
            value={form.macAddressEthernet}
            onChange={(event) => updateField('macAddressEthernet', event.target.value)}
            placeholder="AA:BB:CC:DD:EE:FF"
          />
        </label>
        <label>
          Responsavel
          <input value={form.responsavel} onChange={(event) => updateField('responsavel', event.target.value)} />
        </label>
        <label>
          Setor
          <input value={form.setor} onChange={(event) => updateField('setor', event.target.value)} />
        </label>
        <label className="full-width">
          Categoria
          <input value={form.categoria} onChange={(event) => updateField('categoria', event.target.value)} />
        </label>
        <label className="full-width">
          Observacoes
          <textarea
            rows={5}
            value={form.observacoes}
            onChange={(event) => updateField('observacoes', event.target.value)}
          />
        </label>
      </div>
      <div className="header-actions modal-actions">
        <button className="ghost-button" type="button" onClick={onCancel}>
          Cancelar
        </button>
        <button className="primary-button" type="submit" disabled={saving}>
          {saving ? savingLabel : submitLabel}
        </button>
      </div>
    </form>
  )
}
