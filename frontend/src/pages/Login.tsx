import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { API } from '../services/api'

export function Login() {
  const { user, refreshAuth } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (user) {
      navigate('/dashboard', { replace: true })
    }
  }, [navigate, user])

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setSubmitting(true)
    try {
      const response = await API.login(username, password)
      if (!response.success) {
        throw new Error(response.message ?? 'Credenciais invalidas')
      }
      await refreshAuth()
      const redirectTo = (location.state as { from?: string } | null)?.from || '/dashboard'
      navigate(redirectTo, { replace: true })
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Falha ao autenticar')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="login-screen">
      <div className="login-panel login-panel-compact">
        <form className="form-card" onSubmit={handleSubmit}>
          <div className="login-copy">
            <span className="brand-badge">Controle de ativos</span>
            <h2>Entrar</h2>
            <p>Acesse com seu usuario e senha para continuar.</p>
          </div>
          <div className="form-row single">
            <label>
              Usuario
              <input
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                placeholder="Digite seu usuario"
                autoComplete="username"
                required
              />
            </label>
          </div>
          <div className="form-row single">
            <label>
              Senha
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Digite sua senha"
                autoComplete="current-password"
                required
              />
            </label>
          </div>
          <button type="submit" className="primary-button login-submit" disabled={submitting}>
            {submitting ? 'Entrando...' : 'Entrar'}
          </button>
        </form>
      </div>
    </div>
  )
}
