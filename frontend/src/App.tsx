import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './components/AppShell'
import { AdminRoute, ProtectedRoute } from './components/ProtectedRoute'
import { AuthProvider } from './hooks/useAuth'
import { AtivoDetailsPage } from './pages/AtivoDetailsPage'
import { AtivoFormPage } from './pages/AtivoFormPage'
import { AtivosPage } from './pages/AtivosPage'
import { AuditPage } from './pages/AuditPage'
import { Dashboard } from './pages/Dashboard'
import { Login } from './pages/Login'
import { PerifericosPage } from './pages/PerifericosPage'
import { UsersPage } from './pages/UsersPage'

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<Login />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AppShell />}>
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/ativos" element={<AtivosPage />} />
            <Route path="/ativos/novo" element={<AtivoFormPage />} />
            <Route path="/ativos/:id" element={<AtivoDetailsPage />} />
            <Route path="/ativos/:id/editar" element={<AtivoFormPage />} />
            <Route path="/perifericos" element={<PerifericosPage />} />

            <Route element={<AdminRoute />}>
              <Route path="/usuarios" element={<UsersPage />} />
              <Route path="/auditoria" element={<AuditPage />} />
            </Route>
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AuthProvider>
  )
}

export default App
