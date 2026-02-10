
(function () {
  const path = window.location.pathname.replace(/\/$/, '') || '/';
  const isLogin = path === '/login' || path.endsWith('login.html');
  const isIndex = path === '/' || path.endsWith('index.html');
  const isCadastro = path === '/cadastro' || path.endsWith('cadastro.html');
  const isEditar = path === '/editar' || path.endsWith('editar.html');
  const isVisualizar = path === '/visualizar' || path.endsWith('visualizar.html');

  function getQueryId() {
    const params = new URLSearchParams(window.location.search);
    return params.get('id') || '';
  }

  function redirectLogin() {
    window.location.href = '/login';
  }

  function redirectIndex() {
    window.location.href = '/';
  }

  async function requireAuth() {
    try {
      const st = await API.getAuthStatus();
      if (st && st.authenticated) return st;
    } catch (_) {
    
    }
    redirectLogin();
    return false;
  }

  // ---------- LOGIN ----------
  async function initLogin() {
    try {
      const st = await API.getAuthStatus();
      if (st && st.authenticated) {
        window.location.replace('/');
        return;
      }
    } catch (_) {
      
    }
    const form = document.querySelector('form');
    if (!form) return;

    let submitting = false;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (submitting) return;
      submitting = true;

      const username = document.getElementById('username').value.trim();
      const password = document.getElementById('password').value;
      if (!username || !password) {
        alert('Preencha usuário e senha.');
        submitting = false;
        return;
      }

      const btn = form.querySelector('button[type="submit"]');
      const btnText = btn.innerHTML;
      btn.disabled = true;
      btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Entrando...';

      try {
        const data = await API.login(username, password);
        if (!data.success) {
          alert(data.message || 'Credenciais inválidas.');
          btn.disabled = false;
          btn.innerHTML = btnText;
          submitting = false;
          return;
        }

        // Só redireciona quando a sessão estiver visível
        for (let i = 0; i < 10; i++) {
          await new Promise(r => setTimeout(r, 150));
          try {
            const st = await API.getAuthStatus();
            if (st && st.authenticated) {
              window.location.replace('/');
              return;
            }
          } catch (_) {}
        }
        alert('Sessão não reconhecida. Tente novamente.');
        btn.disabled = false;
        btn.innerHTML = btnText;
        submitting = false;
      } catch (err) {
        console.error(err);
        alert('Erro: ' + (err.message || 'Erro desconhecido ao conectar.'));
        btn.disabled = false;
        btn.innerHTML = btnText;
        submitting = false;
      }
    });
  }

  // ---------- INDEX (lista) ----------
  const PAGE_SIZE = 10;
  let allAtivos = [];
  let currentPage = 1;

  function statusLabel(s) {
    const map = { OPERACIONAL: 'Operacional', ESTOQUE: 'Estoque', MANUTENCAO: 'Manutenção', RESERVADO: 'Reservado', EXTRAVIADO: 'Extraviado' };
    return map[s] || s;
  }

  function renderTable(ativos) {
    const tbody = document.querySelector('.table-container tbody');
    if (!tbody) return;
    if (!ativos.length) {
      tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding: 2rem;">Nenhum ativo encontrado.</td></tr>';
      return;
    }
    tbody.innerHTML = ativos.map(a => `
      <tr>
        <td>${escapeHtml(a.nomeAtivo || '-')}</td>
        <td>${escapeHtml(a.patrimonio || '-')}</td>
        <td><span class="badge">${statusLabel(a.status)}</span></td>
        <td>${escapeHtml(a.localidade || '-')}</td>
        <td>${escapeHtml(a.responsavel || '-')}</td>
        <td>
          <a href="/visualizar?id=${a.id}" class="btn btn-sm btn-secondary"><i class="fas fa-eye"></i></a>
          <a href="/editar?id=${a.id}" class="btn btn-sm btn-secondary"><i class="fas fa-edit"></i></a>
          <button type="button" class="btn btn-sm btn-danger btn-delete" data-id="${a.id}"><i class="fas fa-trash"></i></button>
        </td>
      </tr>
    `).join('');

    tbody.querySelectorAll('.btn-delete').forEach(btn => {
      btn.addEventListener('click', () => deleteAtivo(btn.dataset.id));
    });
  }

  function escapeHtml(t) {
    if (t == null) return '';
    const div = document.createElement('div');
    div.textContent = t;
    return div.innerHTML;
  }

  function updateStats(ativos) {
    const total = ativos.length;
    const operacional = ativos.filter(a => a.status === 'OPERACIONAL').length;
    const estoque = ativos.filter(a => a.status === 'ESTOQUE').length;
    const manutencao = ativos.filter(a => a.status === 'MANUTENCAO').length;
    const el = id => document.getElementById(id);
    if (el('stat-total')) el('stat-total').textContent = total;
    if (el('stat-operacional')) el('stat-operacional').textContent = operacional;
    if (el('stat-estoque')) el('stat-estoque').textContent = estoque;
    if (el('stat-manutencao')) el('stat-manutencao').textContent = manutencao;
  }

  function paginate(list) {
    const start = (currentPage - 1) * PAGE_SIZE;
    return list.slice(start, start + PAGE_SIZE);
  }

  function updatePagination(total) {
    const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
    const prev = document.getElementById('prev-page');
    const next = document.getElementById('next-page');
    const info = document.getElementById('page-info');
    if (prev) prev.disabled = currentPage <= 1;
    if (next) next.disabled = currentPage >= totalPages;
    if (info) info.textContent = `Página ${currentPage} de ${totalPages}`;
  }

  async function deleteAtivo(id) {
    if (!confirm('Deseja realmente excluir este ativo?')) return;
    try {
      await API.deleteAtivo(id);
      allAtivos = allAtivos.filter(a => a.id !== id);
      updateStats(allAtivos);
      renderTable(paginate(allAtivos));
      updatePagination(allAtivos.length);
    } catch (err) {
      alert(err.message || 'Erro ao excluir.');
    }
  }

  async function initIndex() {
    const auth = await requireAuth();
    if (!auth) return;

    const userEl = document.getElementById('user-display');
    if (userEl && auth.user) userEl.textContent = auth.user.username || auth.user.nomeCompleto || 'Usuário';

    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) logoutBtn.addEventListener('click', async (e) => { e.preventDefault(); await API.logout(); redirectLogin(); });

    try {
      allAtivos = await API.getAtivos();
      updateStats(allAtivos);
      renderTable(paginate(allAtivos));
      updatePagination(allAtivos.length);
    } catch (err) {
      if (err.message === 'Não autorizado') redirectLogin();
      else document.querySelector('.table-container tbody').innerHTML = '<tr><td colspan="6" style="text-align:center;">Erro ao carregar ativos.</td></tr>';
      return;
    }

    const searchInput = document.getElementById('search');
    if (searchInput) {
      let debounce;
      searchInput.addEventListener('input', () => {
        clearTimeout(debounce);
        debounce = setTimeout(async () => {
          const termo = searchInput.value.trim();
          try {
            allAtivos = termo ? await API.searchAtivos(termo) : await API.getAtivos();
            currentPage = 1;
            updateStats(allAtivos);
            renderTable(paginate(allAtivos));
            updatePagination(allAtivos.length);
          } catch (_) {}
        }, 300);
      });
    }

    const prevBtn = document.getElementById('prev-page');
    const nextBtn = document.getElementById('next-page');
    if (prevBtn) prevBtn.addEventListener('click', () => { if (currentPage > 1) { currentPage--; renderTable(paginate(allAtivos)); updatePagination(allAtivos.length); } });
    if (nextBtn) nextBtn.addEventListener('click', () => { if (currentPage < Math.ceil(allAtivos.length / PAGE_SIZE)) { currentPage++; renderTable(paginate(allAtivos)); updatePagination(allAtivos.length); } });
  }

  // ---------- CADASTRO ----------
  async function initCadastro() {
    const auth = await requireAuth();
    if (!auth) return;

    const form = document.querySelector('form');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const ativo = {
        nomeAtivo: document.getElementById('nomeAtivo').value.trim(),
        patrimonio: document.getElementById('patrimonio').value.trim(),
        status: document.getElementById('status').value,
        localidade: document.getElementById('localidade').value.trim() || null,
        setor: document.getElementById('setor').value.trim() || null,
        responsavel: document.getElementById('responsavel').value.trim() || null,
      };
      if (!ativo.nomeAtivo || !ativo.patrimonio) {
        alert('Nome e patrimônio são obrigatórios.');
        return;
      }
      const btn = form.querySelector('button[type="submit"]');
      btn.disabled = true;
      try {
        await API.createAtivo(ativo);
        redirectIndex();
      } catch (err) {
        alert(err.message || 'Erro ao salvar.');
        btn.disabled = false;
      }
    });
  }

  // ---------- EDITAR ----------
  async function initEditar() {
    const auth = await requireAuth();
    if (!auth) return;

    const id = getQueryId();
    if (!id) {
      alert('ID do ativo não informado.');
      redirectIndex();
      return;
    }

    try {
      const ativo = await API.getAtivoById(id);
      document.getElementById('nomeAtivo').value = ativo.nomeAtivo || '';
      document.getElementById('patrimonio').value = ativo.patrimonio || '';
      document.getElementById('status').value = ativo.status || 'OPERACIONAL';
      document.getElementById('localidade').value = ativo.localidade || '';
      document.getElementById('setor').value = ativo.setor || '';
      document.getElementById('responsavel').value = ativo.responsavel || '';
    } catch (err) {
      alert(err.message || 'Ativo não encontrado.');
      redirectIndex();
      return;
    }

    const form = document.querySelector('form');
    if (!form) return;
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        nomeAtivo: document.getElementById('nomeAtivo').value.trim(),
        patrimonio: document.getElementById('patrimonio').value.trim(),
        status: document.getElementById('status').value,
        localidade: document.getElementById('localidade').value.trim() || null,
        setor: document.getElementById('setor').value.trim() || null,
        responsavel: document.getElementById('responsavel').value.trim() || null,
      };
      if (!payload.nomeAtivo || !payload.patrimonio) {
        alert('Nome e patrimônio são obrigatórios.');
        return;
      }
      const btn = form.querySelector('button[type="submit"]');
      btn.disabled = true;
      try {
        await API.updateAtivo(id, payload);
        redirectIndex();
      } catch (err) {
        alert(err.message || 'Erro ao atualizar.');
        btn.disabled = false;
      }
    });
  }

  // ---------- VISUALIZAR ----------
  async function initVisualizar() {
    const auth = await requireAuth();
    if (!auth) return;

    const id = getQueryId();
    if (!id) {
      alert('ID do ativo não informado.');
      redirectIndex();
      return;
    }

    try {
      const ativo = await API.getAtivoById(id);
      document.getElementById('view-nome').textContent = ativo.nomeAtivo || '-';
      document.getElementById('view-status').textContent = statusLabel(ativo.status);
      document.getElementById('view-patrimonio').textContent = ativo.patrimonio || '-';
      document.getElementById('view-local').textContent = ativo.localidade || '-';
      document.getElementById('view-setor').textContent = ativo.setor || '-';
      document.getElementById('view-responsavel').textContent = ativo.responsavel || '-';
      const editLink = document.getElementById('edit-link');
      if (editLink) editLink.href = '/editar?id=' + id;
    } catch (err) {
      alert(err.message || 'Ativo não encontrado.');
      redirectIndex();
    }
  }

  // ---------- ROTEAMENTO ----------
  if (isLogin) initLogin();
  else if (isIndex) initIndex();
  else if (isCadastro) initCadastro();
  else if (isEditar) initEditar();
  else if (isVisualizar) initVisualizar();
})();
