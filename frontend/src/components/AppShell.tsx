import { useEffect, useState } from 'react'
import {
  Boxes,
  ClipboardList,
  LayoutDashboard,
  LogOut,
  Menu,
  MonitorSmartphone,
  X,
  UserRound,
  Users,
} from 'lucide-react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

function LiveClock() {
  const [now, setNow] = useState(new Date())

  useEffect(() => {
    const timer = window.setInterval(() => setNow(new Date()), 1000)
    return () => window.clearInterval(timer)
  }, [])

  return (
    <div className="clock-box">
      <span className="clock-label">Horario atual</span>
      <strong>{now.toLocaleTimeString('pt-BR')}</strong>
      <small>{now.toLocaleDateString('pt-BR')}</small>
    </div>
  )
}

export function AppShell() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const isAdmin = user?.role === 'ADMIN'
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const menuItems = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, adminOnly: false },
    { to: '/ativos', label: 'Ativos', icon: Boxes, adminOnly: false },
    { to: '/perifericos', label: 'Perifericos', icon: MonitorSmartphone, adminOnly: false },
    { to: '/usuarios', label: 'Usuarios', icon: Users, adminOnly: true },
    { to: '/auditoria', label: 'Auditoria', icon: ClipboardList, adminOnly: true },
  ]

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  useEffect(() => {
    if (!sidebarOpen) return

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [sidebarOpen])

  return (
    <div className="app-shell">
      <div className="mobile-topbar">
        <button
          type="button"
          className="mobile-menu-button"
          aria-label={sidebarOpen ? 'Fechar menu' : 'Abrir menu'}
          aria-expanded={sidebarOpen}
          onClick={() => setSidebarOpen((current) => !current)}
        >
          {sidebarOpen ? <X size={20} strokeWidth={2.2} /> : <Menu size={20} strokeWidth={2.2} />}
        </button>

        <div className="mobile-topbar-copy">
          <span className="brand-badge">Controle de ativos</span>
          <strong>{user?.nome || user?.username}</strong>
        </div>
      </div>

      {sidebarOpen && <button type="button" className="sidebar-overlay" aria-label="Fechar menu" onClick={() => setSidebarOpen(false)} />}

      <aside className={sidebarOpen ? 'sidebar sidebar-open' : 'sidebar'}>
        <div className="sidebar-top">
          <div className="brand-block">
            <span className="brand-badge">Controle de ativos</span>
            <div className="brand-copy">
              <h1>Painel operacional</h1>
              <p>Fluxo principal</p>
            </div>
          </div>

          <LiveClock />

          <nav className="menu-list" aria-label="Painel operacional">
            {menuItems.map(({ to, label, icon: Icon, adminOnly }) => {
              if (adminOnly && !isAdmin) {
                return null
              }

              return (
                <NavLink
                  key={to}
                  to={to}
                  className={({ isActive }) => isActive ? 'menu-link active' : 'menu-link'}
                  onClick={() => setSidebarOpen(false)}
                >
                  <Icon size={16} strokeWidth={2} />
                  <span>{label}</span>
                </NavLink>
              )
            })}
          </nav>
        </div>

        <div className="session-card">
          <span className="session-label">Sessao</span>
          <div className="profile-card">
            <span className="profile-avatar">
              <UserRound size={18} strokeWidth={2.2} />
            </span>
            <div>
              <strong>{user?.nome || user?.username}</strong>
              <p>{user?.role === 'ADMIN' ? 'ADMIN' : 'USER'}</p>
            </div>
          </div>

          <button type="button" className="logout-button" onClick={handleLogout}>
            <LogOut size={16} strokeWidth={2.1} />
            <span>Sair</span>
          </button>
        </div>
      </aside>

      <main className="content-area">
        <Outlet />
      </main>
    </div>
  )
}
