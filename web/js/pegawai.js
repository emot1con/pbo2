document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    if (!token) {
        window.location.href = '/login';
        return;
    }

    if (role !== 'PEGAWAI') {
        window.location.href = '/admin';
        return;
    }

    const userName = document.getElementById('userName');
    const userRole = document.getElementById('userRole');
    const btnLogout = document.getElementById('btnLogout');
    const alertBox = document.getElementById('alertBox');

    const btnDatang = document.getElementById('btnDatang');
    const statusDatang = document.getElementById('statusDatang');
    const btnPulang = document.getElementById('btnPulang');
    const statusPulang = document.getElementById('statusPulang');

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

    // Load User Info
    async function loadUserInfo() {
        try {
            const response = await fetch('/api/auth/me', {
                headers: { 'Authorization': 'Bearer ' + token }
            });
            if (response.ok) {
                const data = await response.json();
                userName.textContent = data.nama;
                userRole.textContent = `${data.jabatan} - Divisi ${data.divisi}`;
            } else {
                handleSessionExpired();
            }
        } catch (e) {
            showAlert('Gagal memuat profil pengguna', 'alert-danger');
        }
    }

    // Load Attendance Status
    async function loadAttendanceStatus() {
        try {
            const response = await fetch('/api/presensi/status', {
                headers: { 'Authorization': 'Bearer ' + token }
            });

            if (response.ok) {
                const data = await response.json();
                updateAttendanceUI(data);
            } else {
                showAlert('Gagal memuat status presensi', 'alert-danger');
            }
        } catch (e) {
            showAlert('Gagal memuat status presensi', 'alert-danger');
        }
    }

    function updateAttendanceUI(status) {
        // Datang Card logic
        if (status.sudah_datang) {
            btnDatang.disabled = true;
            btnDatang.textContent = 'Sudah Datang';
            
            const badgeClass = status.status_masuk === 'TEPAT_WAKTU' ? 'badge-tepat' : 'badge-terlambat';
            statusDatang.innerHTML = `Jam Masuk: <strong>${status.jam_masuk}</strong> <br> <span class="badge ${badgeClass}">${status.status_masuk.replace('_', ' ')}</span>`;
        } else {
            btnDatang.disabled = false;
            btnDatang.textContent = 'Presensi Datang';
            statusDatang.textContent = 'Belum presensi masuk';
        }

        // Pulang Card logic
        if (status.sudah_pulang) {
            btnPulang.disabled = true;
            btnPulang.textContent = 'Sudah Pulang';
            statusPulang.innerHTML = `Jam Pulang: <strong>${status.jam_keluar}</strong> <br> <span class="badge badge-tepat">TEPAT WAKTU</span>`;
        } else {
            // Disabled if not checked in yet
            if (!status.sudah_datang) {
                btnPulang.disabled = true;
                btnPulang.textContent = 'Presensi Pulang';
                statusPulang.textContent = 'Harus absen datang terlebih dahulu';
            } else {
                // Already checked in, check the time
                const now = new Date();
                const hours = now.getHours();
                
                if (hours < 15) {
                    btnPulang.disabled = true;
                    btnPulang.textContent = 'Presensi Pulang';
                    statusPulang.innerHTML = `<span style="color: var(--warning-color);">Belum waktunya pulang (Minimal pukul 15:00)</span>`;
                } else {
                    btnPulang.disabled = false;
                    btnPulang.textContent = 'Presensi Pulang';
                    statusPulang.textContent = 'Silakan lakukan absensi pulang';
                }
            }
        }
    }

    // Handle Check-in Click
    btnDatang.addEventListener('click', async () => {
        clearAlert();
        btnDatang.disabled = true;
        btnDatang.textContent = 'Memproses...';

        try {
            const response = await fetch('/api/presensi/datang', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token }
            });
            const data = await response.json();

            if (response.ok) {
                showAlert('Presensi datang berhasil!', 'alert-success');
                loadAttendanceStatus();
            } else {
                showAlert(data.error || 'Gagal melakukan presensi datang', 'alert-danger');
                btnDatang.disabled = false;
                btnDatang.textContent = 'Presensi Datang';
            }
        } catch (error) {
            showAlert('Gagal terhubung ke server', 'alert-danger');
            btnDatang.disabled = false;
            btnDatang.textContent = 'Presensi Datang';
        }
    });

    // Handle Check-out Click
    btnPulang.addEventListener('click', async () => {
        clearAlert();
        btnPulang.disabled = true;
        btnPulang.textContent = 'Memproses...';

        try {
            const response = await fetch('/api/presensi/pulang', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token }
            });
            const data = await response.json();

            if (response.ok) {
                showAlert('Presensi pulang berhasil!', 'alert-success');
                loadAttendanceStatus();
            } else {
                showAlert(data.error || 'Gagal melakukan presensi pulang', 'alert-danger');
                btnPulang.disabled = false;
                btnPulang.textContent = 'Presensi Pulang';
            }
        } catch (error) {
            showAlert('Gagal terhubung ke server', 'alert-danger');
            btnPulang.disabled = false;
            btnPulang.textContent = 'Presensi Pulang';
        }
    });

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

    // Run
    loadUserInfo();
    loadAttendanceStatus();
});
