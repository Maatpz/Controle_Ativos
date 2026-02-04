/**
 * Cliente API - Controle de Ativos
 * Base URL relativa ao mesmo origin (Spring Boot serve front e API).
 * credentials: 'include' envia o cookie JSESSIONID para autenticação.
 */
const API = {
  base: '',
  opts(method, body) {
    const o = {
      method,
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
    };
    if (body) o.body = JSON.stringify(body);
    return o;
  },

  // --- Auth ---
  async login(username, password) {
    const res = await fetch(`${this.base}/auth/login`, this.opts('POST', { username, password }));
    return res.json();
  },
  async logout() {
    const res = await fetch(`${this.base}/auth/logout`, this.opts('POST'));
    return res.json();
  },
  async getAuthStatus() {
    const res = await fetch(`${this.base}/auth/status`, { method: 'GET', credentials: 'include' });
    return res.json();
  },

  // --- Ativos ---
  async getAtivos() {
    const res = await fetch(`${this.base}/ativos`, { method: 'GET', credentials: 'include' });
    if (!res.ok) throw new Error(res.status === 401 ? 'Não autorizado' : 'Erro ao listar ativos');
    return res.json();
  },
  async getAtivoById(id) {
    const res = await fetch(`${this.base}/ativos/${id}`, { method: 'GET', credentials: 'include' });
    if (!res.ok) throw new Error(res.status === 404 ? 'Ativo não encontrado' : 'Erro ao buscar ativo');
    return res.json();
  },
  async createAtivo(ativo) {
    const res = await fetch(`${this.base}/ativos`, this.opts('POST', ativo));
    if (!res.ok) throw new Error('Erro ao criar ativo');
    return res.json();
  },
  async updateAtivo(id, ativo) {
    const res = await fetch(`${this.base}/ativos/${id}`, this.opts('PUT', ativo));
    if (!res.ok) throw new Error(res.status === 404 ? 'Ativo não encontrado' : 'Erro ao atualizar');
    return res.json();
  },
  async deleteAtivo(id) {
    const res = await fetch(`${this.base}/ativos/${id}`, { method: 'DELETE', credentials: 'include' });
    if (!res.ok) throw new Error(res.status === 404 ? 'Ativo não encontrado' : 'Erro ao excluir');
    return res;
  },
  async searchAtivos(termo) {
    const q = termo ? `?termo=${encodeURIComponent(termo)}` : '';
    const res = await fetch(`${this.base}/ativos/search${q}`, { method: 'GET', credentials: 'include' });
    if (!res.ok) throw new Error('Erro na busca');
    return res.json();
  },
};
