const API_BASE = window.location.origin;

const api = {
    async login(username, password) {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        return response.json();
    },

    async logout() {
        const response = await fetch(`${API_BASE}/auth/logout`, { method: 'POST' });
        return response.json();
    },

    async checkAuth() {
        try {
            const response = await fetch(`${API_BASE}/auth/status`);
            return response.json();
        } catch (e) {
            return { authenticated: false };
        }
    },

    async getAtivos(page = 0, size = 10) {
        const response = await fetch(`${API_BASE}/ativos?page=${page}&size=${size}`);
        return response.json();
    },

    async getAtivo(id) {
        const response = await fetch(`${API_BASE}/ativos/${id}`);
        if (!response.ok) throw new Error('Ativo not found');
        return response.json();
    },

    async createAtivo(ativo) {
        const response = await fetch(`${API_BASE}/ativos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ativo)
        });
        if (!response.ok) throw new Error('Failed to create ativo');
        return response.json();
    },

    async updateAtivo(id, ativo) {
        const response = await fetch(`${API_BASE}/ativos/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ativo)
        });
        if (!response.ok) throw new Error('Failed to update ativo');
        return response.json();
    },

    async deleteAtivo(id) {
        const response = await fetch(`${API_BASE}/ativos/${id}`, {
            method: 'DELETE'
        });
        if (!response.ok) throw new Error('Failed to delete ativo');
        return true;
    },

    // Search methods
    async search(params) {
        // Construct query string
        const searchParams = new URLSearchParams();
        Object.keys(params).forEach(key => {
            if (params[key]) searchParams.append(key, params[key]);
        });

        const response = await fetch(`${API_BASE}/ativos/search/advanced?${searchParams.toString()}`);
        return response.json();
    }
};

window.api = api;
