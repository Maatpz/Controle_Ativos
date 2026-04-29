import axios from 'axios'

const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

let csrfRequest: Promise<void> | null = null
let csrfToken: string | null = null
let csrfHeaderName = 'X-XSRF-TOKEN'

export type Role = 'ADMIN' | 'USER'
export type Status = 'OPERACIONAL' | 'ESTOQUE' | 'MANUTENCAO'
export type TipoPeriferico = 'KIT_MOUSE_TECLADO' | 'ADAPTADOR' | 'FONE' | 'OUTRO'

export type AuthUser = {
  id: string
  username: string
  nome: string
  role: Role
}

export type Ativo = {
  id?: string
  nomeAtivo: string
  setor: string
  responsavel: string
  categoria: string
  patrimonio: string
  status: Status
  macAddressEthernet: string
  observacoes: string
  createdAt?: string
  updatedAt?: string
}

export type Usuario = {
  id: string
  username: string
  nome: string
  role: Role
  ativo: boolean
  createdAt?: string
}

export type AuditoriaLog = {
  id: string
  entidade: string
  entidadeId: string
  acao: string
  usuario: string
  perfilUsuario: string
  detalhes: string
  createdAt: string
}

export type Periferico = {
  id?: string
  nome: string
  tipo: TipoPeriferico
  quantidade: number
  observacoes?: string
  createdAt?: string
  updatedAt?: string
}

export type DashboardData = {
  ativos: {
    totalAtivos: number
    operacionais: number
    estoque: number
    manutencao: number
    porSetor: Array<{ nome: string; total: number }>
    porCategoria: Array<{ nome: string; total: number }>
    porStatus: Array<{ nome: string; total: number }>
  }
  perifericos: {
    porTipo: Array<{ nome: string; total: number }>
  }
  usuarios?: {
    totalUsuarios: number
    admins: number
    users: number
  }
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

function toMessage(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data
    if (typeof data === 'string') return data
    if (data?.errors) {
      return Object.values(data.errors).join(' | ')
    }
    if (data?.message) return data.message as string
  }
  return fallback
}

function toQuery(params: Record<string, string | undefined>) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value && value.trim()) {
      search.set(key, value.trim())
    }
  })
  const query = search.toString()
  return query ? `?${query}` : ''
}

async function ensureCsrfToken(forceRefresh = false) {
  if (!csrfRequest || forceRefresh) {
    csrfRequest = apiClient.get('/api/auth/csrf')
      .then((response) => {
        csrfToken = response.data?.token ?? null
        csrfHeaderName = response.data?.headerName ?? 'X-XSRF-TOKEN'
      })
      .catch((error) => {
        csrfRequest = null
        csrfToken = null
        throw error
      })
  }

  await csrfRequest
}

function withCsrfHeaders() {
  return csrfToken
    ? { headers: { [csrfHeaderName]: csrfToken } }
    : undefined
}

export const API = {
  async login(username: string, password: string) {
    try {
      await ensureCsrfToken()
      const response = await apiClient.post('/api/auth/login', { username, password }, withCsrfHeaders())
      await ensureCsrfToken(true)
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao fazer login'))
    }
  },

  async logout() {
    await ensureCsrfToken()
    await apiClient.post('/api/auth/logout', undefined, withCsrfHeaders())
    csrfToken = null
    csrfRequest = null
  },

  async getAuthStatus(): Promise<{ authenticated: boolean; user: AuthUser | null }> {
    try {
      const response = await apiClient.get('/api/auth/status')
      return {
        authenticated: Boolean(response.data?.authenticated),
        user: response.data?.authenticated ? response.data.user as AuthUser : null,
      }
    } catch {
      return { authenticated: false, user: null }
    }
  },

  async getDashboard(): Promise<DashboardData> {
    try {
      const response = await apiClient.get('/dashboard')
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao carregar dashboard'))
    }
  },

  async getAtivos(params?: { page?: number; size?: number; sort?: string; termo?: string; nome?: string; responsavel?: string; patrimonio?: string }): Promise<PageResponse<Ativo>> {
    try {
      const response = await apiClient.get(`/ativos${toQuery({
        page: params?.page?.toString(),
        size: params?.size?.toString(),
        sort: params?.sort,
        termo: params?.termo,
        nome: params?.nome,
        responsavel: params?.responsavel,
        patrimonio: params?.patrimonio,
      })}`)
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao listar ativos'))
    }
  },

  async getAtivoById(id: string): Promise<Ativo> {
    try {
      const response = await apiClient.get(`/ativos/${id}`)
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao carregar ativo'))
    }
  },

  async createAtivo(ativo: Ativo) {
    try {
      await ensureCsrfToken()
      const response = await apiClient.post('/ativos', ativo, withCsrfHeaders())
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao criar ativo'))
    }
  },

  async updateAtivo(id: string, ativo: Ativo) {
    try {
      await ensureCsrfToken()
      const response = await apiClient.put(`/ativos/${id}`, ativo, withCsrfHeaders())
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao atualizar ativo'))
    }
  },

  async deleteAtivo(id: string) {
    try {
      await ensureCsrfToken()
      await apiClient.delete(`/ativos/${id}`, withCsrfHeaders())
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao excluir ativo'))
    }
  },

  async exportTxt(filters?: { termo?: string; nome?: string; responsavel?: string; patrimonio?: string }) {
    try {
      const response = await apiClient.get(`/ativos/export/txt${toQuery(filters ?? {})}`, { responseType: 'blob' })
      const blob = new Blob([response.data], { type: 'text/plain;charset=utf-8' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `ativos-${new Date().toISOString().slice(0, 10)}.txt`
      link.click()
      window.URL.revokeObjectURL(url)
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao exportar ativos'))
    }
  },

  async getUsuarios(params?: { page?: number; size?: number; sort?: string }): Promise<PageResponse<Usuario>> {
    try {
      const response = await apiClient.get(`/usuarios${toQuery({
        page: params?.page?.toString(),
        size: params?.size?.toString(),
        sort: params?.sort,
      })}`)
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao listar usuarios'))
    }
  },

  async getUsuariosResumo(): Promise<{ totalUsuarios: number; admins: number; users: number }> {
    try {
      const response = await apiClient.get('/usuarios/resumo')
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao carregar resumo de usuarios'))
    }
  },

  async createUsuario(payload: { username: string; nome: string; password: string; role: Role; ativo: boolean }) {
    try {
      await ensureCsrfToken()
      const response = await apiClient.post('/usuarios', payload, withCsrfHeaders())
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao criar usuario'))
    }
  },

  async updateUsuario(id: string, payload: { username: string; nome: string; password?: string; role: Role; ativo: boolean }) {
    try {
      await ensureCsrfToken()
      const response = await apiClient.put(`/usuarios/${id}`, payload, withCsrfHeaders())
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao atualizar usuario'))
    }
  },

  async deleteUsuario(id: string) {
    try {
      await ensureCsrfToken()
      await apiClient.delete(`/usuarios/${id}`, withCsrfHeaders())
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao excluir usuario'))
    }
  },

  async getAuditorias(params?: { page?: number; size?: number }): Promise<PageResponse<AuditoriaLog>> {
    try {
      const response = await apiClient.get(`/auditorias${toQuery({
        page: params?.page?.toString(),
        size: params?.size?.toString(),
      })}`)
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao carregar auditoria'))
    }
  },

  async getPerifericos(params?: { page?: number; size?: number; sort?: string }): Promise<PageResponse<Periferico>> {
    try {
      const response = await apiClient.get(`/perifericos${toQuery({
        page: params?.page?.toString(),
        size: params?.size?.toString(),
        sort: params?.sort,
      })}`)
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao listar perifericos'))
    }
  },

  async createPeriferico(payload: Periferico) {
    try {
      await ensureCsrfToken()
      const response = await apiClient.post('/perifericos', payload, withCsrfHeaders())
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao criar periferico'))
    }
  },

  async updatePeriferico(id: string, payload: Periferico) {
    try {
      await ensureCsrfToken()
      const response = await apiClient.put(`/perifericos/${id}`, payload, withCsrfHeaders())
      return response.data
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao atualizar periferico'))
    }
  },

  async deletePeriferico(id: string) {
    try {
      await ensureCsrfToken()
      await apiClient.delete(`/perifericos/${id}`, withCsrfHeaders())
    } catch (error) {
      throw new Error(toMessage(error, 'Erro ao excluir periferico'))
    }
  },
}
