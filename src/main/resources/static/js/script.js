document.addEventListener('DOMContentLoaded', async () => {
    const path = window.location.pathname;

    // Auth Check
    const authStatus = await api.checkAuth();
    if (!authStatus.authenticated && path !== '/login.html' && path !== '/login') {
        window.location.href = '/login';
        return;
    }

    if (authStatus.authenticated && (path === '/login.html' || path === '/login')) {
        window.location.href = '/';
        return;
    }

    // Setup Navigation User Info
    if (authStatus.authenticated) {
        const userDisplay = document.getElementById('user-display');
        if (userDisplay) userDisplay.textContent = authStatus.user.nomeCompleto || authStatus.user.username;

        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', async (e) => {
                e.preventDefault();
                await api.logout();
                window.location.href = '/login';
            });
        }
    }

    // Page Specific Logic
    if (path === '/' || path === '/index.html' || path === '') {
        loadDashboard();
    } else if (path === '/cadastro' || path === '/cadastro.html') {
        setupForm();
    } else if (path === '/editar' || path === '/editar.html') {
        loadEditForm();
    } else if (path === '/visualizar' || path === '/visualizar.html') {
        loadVisualizar();
    } else if (path === '/login' || path === '/login.html') {
        setupLogin();
    }
});

// Toast Notification
function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}"></i>
        <span>${message}</span>
    `;
    document.body.appendChild(toast);

    // Trigger animation
    setTimeout(() => toast.classList.add('show'), 10);

    // Remove
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Login Page
function setupLogin() {
    const form = document.querySelector('form');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = form.username.value;
        const password = form.password.value;

        try {
            const result = await api.login(username, password);
            if (result.success) {
                window.location.href = '/';
            } else {
                showToast(result.message || 'Login falhou', 'error');
            }
        } catch (error) {
            showToast('Erro de conexão', 'error');
        }
    });
}

// Dashboard
// Dashboard State
let currentPage = 0;
const pageSize = 10;
let totalPages = 0;

async function loadDashboard(page = 0) {
    try {
        const response = await api.getAtivos(page, pageSize);

        // Handle paginated response structure
        const content = response.content || [];
        totalPages = response.totalPages || 0;
        currentPage = response.number || 0;

        updateStats(content, response.totalElements); // Note: Stats might be partial if not fetched separately, but using current page data or totalElements if available
        renderTable(content);
        updatePaginationControls();

        // Search setup (Client-side filtering is limited now, ideally search should call API)
        const searchInput = document.getElementById('search');
        if (searchInput) {
            // Remove old listener to avoid duplicates if re-initialized? 
            // Better to attach once or use a different approach. 
            // For now, let's just update the search to call the API search endpoint which returns a list (not paginated yet in this refactor, but compatible)

            searchInput.oninput = async (e) => {
                const term = e.target.value;
                if (term.length > 2) {
                    const searchResults = await api.search({ termo: term }); // Using advanced search or general term
                    // API search currently returns List<DTO>, not Page. So we render it differently.
                    renderTable(searchResults);
                    document.getElementById('pagination-controls').style.display = 'none';
                } else if (term.length === 0) {
                    loadDashboard(0);
                    document.getElementById('pagination-controls').style.display = 'flex';
                }
            };
        }

        // Pagination Event Listeners (Attach once)
        if (!window.paginationListenersAttached) {
            document.getElementById('prev-page').addEventListener('click', () => {
                if (currentPage > 0) loadDashboard(currentPage - 1);
            });
            document.getElementById('next-page').addEventListener('click', () => {
                if (currentPage < totalPages - 1) loadDashboard(currentPage + 1);
            });
            window.paginationListenersAttached = true;
        }

    } catch (e) {
        console.error(e);
        showToast('Erro ao carregar ativos', 'error');
    }
}

function updatePaginationControls() {
    const prevBtn = document.getElementById('prev-page');
    const nextBtn = document.getElementById('next-page');
    const pageInfo = document.getElementById('page-info');

    if (prevBtn) prevBtn.disabled = currentPage === 0;
    if (nextBtn) nextBtn.disabled = currentPage >= totalPages - 1;
    if (pageInfo) pageInfo.textContent = `Página ${currentPage + 1} de ${totalPages || 1}`;
}

function updateStats(ativos, totalElements) {
    // Note: With pagination, we can't calculate full stats from just one page. 
    // Ideally we need a separate stats API. 
    // For now, we will just show Total Elements if available, and dashes for specific counts unless we fetch all.
    // Changing behavior to be consistent with pagination limitations.

    if (totalElements !== undefined) {
        document.getElementById('stat-total').textContent = totalElements;
    } else {
        document.getElementById('stat-total').textContent = ativos.length;
    }

    // Disable other stats for now or set to '-' as we don't have full data
    document.getElementById('stat-operacional').textContent = '-';
    document.getElementById('stat-estoque').textContent = '-';
    document.getElementById('stat-reservado').textContent = '-';
}

function renderTable(ativos) {
    const tbody = document.querySelector('tbody');
    if (!tbody) return;

    if (ativos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Nenhum ativo encontrado</td></tr>';
        return;
    }

    tbody.innerHTML = ativos.map(ativo => `
        <tr>
            <td>${ativo.nomeAtivo}</td>
            <td>${ativo.patrimonio}</td>
            <td><span class="badge badge-${getStatusColor(ativo.status)}">${ativo.status}</span></td>
            <td>${ativo.localidade || '-'}</td>
            <td>${ativo.responsavel || '-'}</td>
            <td>
                <div style="display: flex; gap: 0.5rem;">
                    <a href="/visualizar?id=${ativo.id}" class="btn btn-sm btn-secondary"><i class="fas fa-eye"></i></a>
                    <a href="/editar?id=${ativo.id}" class="btn btn-sm btn-primary"><i class="fas fa-edit"></i></a>
                    <button onclick="deleteAtivo('${ativo.id}')" class="btn btn-sm btn-danger"><i class="fas fa-trash"></i></button>
                </div>
            </td>
        </tr>
    `).join('');
}

function getStatusColor(status) {
    const map = {
        'OPERACIONAL': 'success',
        'MANUTENCAO': 'warning',
        'ESTOQUE': 'info',
        'EXTRAVIADO': 'danger',
        'RESERVADO': 'info'
    };
    return map[status] || 'secondary';
}

async function deleteAtivo(id) {
    if (confirm('Tem certeza que deseja excluir este ativo?')) {
        try {
            await api.deleteAtivo(id);
            showToast('Ativo excluído com sucesso');
            loadDashboard();
        } catch (e) {
            showToast('Erro ao excluir ativo', 'error');
        }
    }
}
// Expose to window for onclick
window.deleteAtivo = deleteAtivo;

// Create/Edit Form
function setupForm() {
    const form = document.querySelector('form');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());

        try {
            await api.createAtivo(data);
            showToast('Ativo criado com sucesso');
            setTimeout(() => window.location.href = '/', 1000);
        } catch (e) {
            console.error(e);
            showToast('Erro ao criar ativo', 'error');
        }
    });
}

async function loadEditForm() {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id');

    if (!id) {
        window.location.href = '/';
        return;
    }

    try {
        const ativo = await api.getAtivo(id);

        // Populate form
        const form = document.querySelector('form');
        for (const key in ativo) {
            const input = form.elements[key];
            if (input) input.value = ativo[key];
        }

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(form);
            const data = Object.fromEntries(formData.entries());
            // Ensure ID is not overwritten or sent if not needed by backendDTO, though PUT usually takes ID from path

            try {
                await api.updateAtivo(id, data);
                showToast('Ativo atualizado com sucesso');
                setTimeout(() => window.location.href = '/', 1000);
            } catch (e) {
                showToast('Erro ao atualizar ativo', 'error');
            }
        });

    } catch (e) {
        showToast('Erro ao carregar dados do ativo', 'error');
    }
}

async function loadVisualizar() {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get('id');

    if (!id) {
        window.location.href = '/';
        return;
    }

    try {
        const ativo = await api.getAtivo(id);

        document.getElementById('view-nome').textContent = ativo.nomeAtivo;
        document.getElementById('view-patrimonio').textContent = ativo.patrimonio;
        document.getElementById('view-status').textContent = ativo.status;
        document.getElementById('view-status').className = `badge badge-${getStatusColor(ativo.status)}`;
        document.getElementById('view-local').textContent = ativo.localidade || '-';
        document.getElementById('view-setor').textContent = ativo.setor || '-';
        document.getElementById('view-responsavel').textContent = ativo.responsavel || '-';

    } catch (e) {
        showToast('Erro ao carregar detalhes', 'error');
    }
}
