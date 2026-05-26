document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    if (!token) {
        window.location.href = '/login';
        return;
    }

    if (role !== 'ADMIN') {
        window.location.href = '/pegawai';
        return;
    }

    const adminName = document.getElementById('adminName');
    const btnLogout = document.getElementById('btnLogout');
    const btnRefresh = document.getElementById('btnRefresh');
    const currentDate = document.getElementById('currentDate');
    const totalHadir = document.getElementById('totalHadir');
    const tableBody = document.getElementById('attendanceTableBody');
    const alertBox = document.getElementById('alertBox');

    // Display current date
    const today = new Date();
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    currentDate.textContent = today.toLocaleDateString('id-ID', options);

    // Setup Admin Name
    const storedName = localStorage.getItem('nama');
    if (storedName) {
        adminName.textContent = storedName;
    }

    // Setup Logout
    btnLogout.addEventListener('click', async () => {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token }
            });
        } catch (e) {}
        localStorage.clear();
        window.location.href = '/login';
    });

    // Refresh data on button click
    btnRefresh.addEventListener('click', () => {
        loadDashboardData();
    });

    // Load dashboard attendance list
    async function loadDashboardData() {
        btnRefresh.disabled = true;
        btnRefresh.textContent = 'Memuat...';
        clearAlert();

        try {
            const response = await fetch('/api/admin/dashboard', {
                headers: { 'Authorization': 'Bearer ' + token }
            });

            if (response.ok) {
                const data = await response.json();
                renderTable(data);
            } else if (response.status === 401) {
                handleSessionExpired();
            } else {
                showAlert('Gagal mengambil data dari server', 'alert-danger');
            }
        } catch (e) {
            showAlert('Gagal terhubung ke server', 'alert-danger');
        } finally {
            btnRefresh.disabled = false;
            btnRefresh.textContent = 'Segarkan';
        }
    }

    function renderTable(list) {
        tableBody.innerHTML = '';
        totalHadir.textContent = list.length;

        if (list.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; color: var(--text-secondary); padding: 30px;">
                        Belum ada pegawai yang hadir hari ini.
                    </td>
                </tr>
            `;
            return;
        }

        list.forEach(row => {
            const tr = document.createElement('tr');
            
            const badgeMasukClass = row.status_masuk === 'TEPAT_WAKTU' ? 'badge-tepat' : 'badge-terlambat';
            const badgePulangClass = row.status_pulang === 'TEPAT_WAKTU' ? 'badge-tepat' : '';
            
            const displayStatusMasuk = row.status_masuk ? row.status_masuk.replace('_', ' ') : '-';
            const displayStatusPulang = row.status_pulang ? row.status_pulang.replace('_', ' ') : '-';

            tr.innerHTML = `
                <td><strong>${escapeHtml(row.nama)}</strong></td>
                <td>${escapeHtml(row.jabatan || '-')}</td>
                <td>${escapeHtml(row.divisi || '-')}</td>
                <td><code>${row.jam_masuk}</code></td>
                <td><code>${row.jam_keluar}</code></td>
                <td><span class="badge ${badgeMasukClass}">${displayStatusMasuk}</span></td>
                <td><span class="badge ${badgePulangClass}">${displayStatusPulang}</span></td>
            `;
            
            tableBody.appendChild(tr);
        });
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;')
                  .replace(/</g, '&lt;')
                  .replace(/>/g, '&gt;')
                  .replace(/"/g, '&quot;')
                  .replace(/'/g, '&#039;');
    }

    function handleSessionExpired() {
        localStorage.clear();
        window.location.href = '/login';
    }

    function showAlert(msg, className) {
        alertBox.textContent = msg;
        alertBox.style.display = 'block';
        alertBox.className = 'alert ' + className;
    }

    function clearAlert() {
        alertBox.style.display = 'none';
        alertBox.className = 'alert';
    }

    // Run first load
    loadDashboardData();

    // Auto refresh every 30 seconds
    setInterval(loadDashboardData, 30000);
});
