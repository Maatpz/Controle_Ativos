
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
          } catch (_) { }
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
  let allAtivosGlobal = [];
  let currentPage = 1;
  let currentSort = { field: null, order: 'none' };

  function statusLabel(s) {
    const map = {
      OPERACIONAL: 'Operacional', ESTOQUE: 'Estoque', MANUTENCAO: 'Manutenção', RESERVADO: 'Reservado',
      // EXTRAVIADO: 'Extraviado' 
    };
    return map[s] || s;
  }

  const emptyStateHtml = '<tr><td colspan="6" class="empty-state-cell"><div class="empty-state"><i class="fas fa-inbox empty-state-icon"></i><p class="empty-state-text">Nenhum ativo encontrado.</p></div></td></tr>';

  const emptyStateMobileHtml = '<div class="mobile-cards-empty"><i class="fas fa-inbox"></i><p>Nenhum ativo encontrado.</p></div>';

  function renderTable(ativos) {
    const tbody = document.querySelector('.table-container tbody');
    const mobileCards = document.getElementById('mobile-cards');
    if (!tbody) return;
    if (!ativos.length) {
      tbody.innerHTML = emptyStateHtml;
      if (mobileCards) mobileCards.innerHTML = emptyStateMobileHtml;
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

    if (mobileCards) {
      mobileCards.innerHTML = ativos.map(a => `
        <div class="mobile-card">
          <div class="mobile-card-header">
            <strong>${escapeHtml(a.nomeAtivo || '-')}</strong>
            <span class="badge">${statusLabel(a.status)}</span>
          </div>
          <div class="mobile-card-body">
            <div>Patrimônio: ${escapeHtml(a.patrimonio || '-')}</div>
            <div>Observações: ${escapeHtml(a.localidade || '-')}</div>
            <div>Responsável: ${escapeHtml(a.responsavel || '-')}</div>
          </div>
          <div class="mobile-card-actions">
            <a href="/visualizar?id=${a.id}" class="btn btn-sm btn-secondary"><i class="fas fa-eye"></i></a>
            <a href="/editar?id=${a.id}" class="btn btn-sm btn-secondary"><i class="fas fa-edit"></i></a>
            <button type="button" class="btn btn-sm btn-danger btn-delete" data-id="${a.id}"><i class="fas fa-trash"></i></button>
          </div>
        </div>
      `).join('');
      mobileCards.querySelectorAll('.btn-delete').forEach(btn => {
        btn.addEventListener('click', () => deleteAtivo(btn.dataset.id));
      });
    }
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

  function naturalCompare(a, b) {
    const strA = (a == null ? '' : a).toString().trim();
    const strB = (b == null ? '' : b).toString().trim();
    const chunksA = strA.match(/(\d+|\D+)/g) || [strA];
    const chunksB = strB.match(/(\d+|\D+)/g) || [strB];
    const len = Math.max(chunksA.length, chunksB.length);
    for (let i = 0; i < len; i++) {
      const partA = chunksA[i] || '';
      const partB = chunksB[i] || '';
      const numA = parseInt(partA, 10);
      const numB = parseInt(partB, 10);
      const aIsNum = !Number.isNaN(numA) && partA === String(numA);
      const bIsNum = !Number.isNaN(numB) && partB === String(numB);
      let cmp;
      if (aIsNum && bIsNum) {
        cmp = numA - numB;
      } else {
        cmp = partA.toLowerCase().localeCompare(partB.toLowerCase(), 'pt-BR');
      }
      if (cmp !== 0) return cmp;
    }
    return 0;
  }

  function handleSort(field) {
    if (currentSort.field === field) {
      currentSort.order = currentSort.order === 'asc' ? 'desc' : 'asc';
    } else {
      currentSort.field = field;
      currentSort.order = 'asc';
    }

    allAtivos.sort((a, b) => {
      let valA = a[field] ?? '';
      let valB = b[field] ?? '';

      if (field === 'status') {
        valA = statusLabel(valA);
        valB = statusLabel(valB);
      }

      let cmp = naturalCompare(valA, valB);

      // Ordenação secundária para evitar "bug" de visualização instável
      if (cmp === 0) {
        const cmpNome = naturalCompare(a.nomeAtivo, b.nomeAtivo);
        if (cmpNome !== 0) return cmpNome;
        return naturalCompare(a.patrimonio, b.patrimonio);
      }

      return currentSort.order === 'desc' ? -cmp : cmp;
    });

    document.querySelectorAll('th.sortable').forEach(th => {
      const thField = th.dataset.sort;
      const icon = th.querySelector('i');
      th.classList.remove('asc', 'desc');
      if (icon) icon.className = 'fas fa-sort';

      if (thField === field) {
        th.classList.add(currentSort.order);
        if (icon) {
          icon.className = currentSort.order === 'asc' ? 'fas fa-sort-up' : 'fas fa-sort-down';
        }
      }
    });

    currentPage = 1;
    renderTable(paginate(allAtivos));
    updatePagination(allAtivos.length);
  }

  function resetSortUI() {
    currentSort = { field: null, order: 'none' };
    document.querySelectorAll('th.sortable').forEach(th => {
      th.classList.remove('asc', 'desc');
      const icon = th.querySelector('i');
      if (icon) icon.className = 'fas fa-sort';
    });
  }

  function updateSearchResultInfo(termo, count) {
    const box = document.getElementById('search-result-info');
    const text = document.getElementById('search-result-text');
    if (!box || !text) return;
    if (!termo) {
      box.style.display = 'none';
      return;
    }
    const label = count === 1 ? '1 ativo encontrado' : `${count} ativos encontrados`;
    text.textContent = `${label} para a busca.`;
    box.style.display = 'flex';
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
      allAtivosGlobal = allAtivosGlobal.filter(a => a.id !== id);
      updateStats(allAtivosGlobal);
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
    const avatarEl = document.getElementById('user-avatar');
    if (userEl && auth.user) {
      const name = auth.user.username || auth.user.nomeCompleto || 'Usuário';
      userEl.textContent = name;
      if (avatarEl && name) avatarEl.textContent = name.charAt(0).toUpperCase();
    }

    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) logoutBtn.addEventListener('click', async (e) => { e.preventDefault(); await API.logout(); redirectLogin(); });

    const btnExportTxt = document.getElementById('btn-export-txt');
    if (btnExportTxt) {
      btnExportTxt.addEventListener('click', async () => {
        const termo = (document.getElementById('search') && document.getElementById('search').value.trim()) || '';
        try {
          btnExportTxt.disabled = true;
          btnExportTxt.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Exportando...';
          await API.exportTxt(termo || undefined);
        } catch (err) {
          alert(err.message || 'Erro ao exportar.');
        } finally {
          btnExportTxt.disabled = false;
          btnExportTxt.innerHTML = '<i class="fas fa-file-alt"></i> Exportar TXT';
        }
      });
    }

    try {
      allAtivos = await API.getAtivos();
      allAtivosGlobal = allAtivos.slice();
      updateStats(allAtivosGlobal);
      renderTable(paginate(allAtivos));
      updatePagination(allAtivos.length);
    } catch (err) {
      if (err.message === 'Não autorizado') redirectLogin();
      else document.querySelector('.table-container tbody').innerHTML = '<tr><td colspan="6" style="text-align:center;">Erro ao carregar ativos.</td></tr>';
      return;
    }

    document.querySelectorAll('th.sortable').forEach(th => {
      th.addEventListener('click', () => {
        handleSort(th.dataset.sort);
      });
    });

    const searchInput = document.getElementById('search');
    if (searchInput) {
      let debounce;
      searchInput.addEventListener('input', () => {
        clearTimeout(debounce);
        debounce = setTimeout(async () => {
          const termo = searchInput.value.trim();
          try {
            if (termo) {
              allAtivos = await API.searchAtivos(termo);
              updateStats(allAtivosGlobal);
              updateSearchResultInfo(termo, allAtivos.length);
            } else {
              allAtivos = await API.getAtivos();
              allAtivosGlobal = allAtivos.slice();
              updateStats(allAtivosGlobal);
              updateSearchResultInfo('', 0);
            }
            resetSortUI();
            currentPage = 1;
            renderTable(paginate(allAtivos));
            updatePagination(allAtivos.length);
          } catch (_) { }
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
