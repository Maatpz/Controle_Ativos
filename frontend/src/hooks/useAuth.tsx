import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { API, type AuthUser } from '../services/api'

type AuthContextValue = {
  user: AuthUser | null
  loading: boolean
  refreshAuth: () => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [loading, setLoading] = useState(true)

  async function refreshAuth() {
    try {
      const status = await API.getAuthStatus()
      setUser(status.authenticated ? status.user : null)
    } finally {
      setLoading(false)
    }
  }

  async function logout() {
    await API.logout()
    setUser(null)
  }

  useEffect(() => {
    void refreshAuth()
  }, [])

  return (
    <AuthContext.Provider value={{ user, loading, refreshAuth, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
