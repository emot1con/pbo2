document.addEventListener('DOMContentLoaded', () => {
    // Clear old token on load
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('nama');

    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const alertBox = document.getElementById('alertBox');
    const btnSubmit = document.getElementById('btnSubmit');

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        // Clear alert
        alertBox.style.display = 'none';
        alertBox.className = 'alert';
        
        const email = emailInput.value.trim();
        const password = passwordInput.value;

        if (!email || !password) {
            showAlert('Email dan password harus diisi', 'alert-danger');
            return;
        }

        btnSubmit.disabled = true;
        btnSubmit.textContent = 'Memproses...';

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                localStorage.setItem('token', data.token);
                localStorage.setItem('role', data.role);
                localStorage.setItem('nama', data.nama);

                showAlert('Login berhasil! Mengalihkan...', 'alert-success');

                setTimeout(() => {
                    if (data.role === 'ADMIN') {
                        window.location.href = '/admin';
                    } else {
                        window.location.href = '/pegawai';
                    }
                }, 1000);
            } else {
                showAlert(data.error || 'Email atau password salah', 'alert-danger');
                btnSubmit.disabled = false;
                btnSubmit.textContent = 'Masuk';
            }
        } catch (error) {
            console.error('Error during login:', error);
            showAlert('Gagal terhubung ke server', 'alert-danger');
            btnSubmit.disabled = false;
            btnSubmit.textContent = 'Masuk';
        }
    });

    function showAlert(msg, className) {
        alertBox.textContent = msg;
        alertBox.style.display = 'block';
        alertBox.className = 'alert ' + className;
    }
});
