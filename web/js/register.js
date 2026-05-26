document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const namaInput = document.getElementById('nama');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const jabatanInput = document.getElementById('jabatan');
    const divisiInput = document.getElementById('divisi');
    const alertBox = document.getElementById('alertBox');
    const btnSubmit = document.getElementById('btnSubmit');

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        alertBox.style.display = 'none';
        alertBox.className = 'alert';

        const nama = namaInput.value.trim();
        const email = emailInput.value.trim();
        const password = passwordInput.value;
        const jabatan = jabatanInput.value.trim();
        const divisi = divisiInput.value.trim();

        btnSubmit.disabled = true;
        btnSubmit.textContent = 'Mendaftarkan...';

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ nama, email, password, jabatan, divisi })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                showAlert('Pendaftaran sukses! Mengalihkan ke login...', 'alert-success');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 1500);
            } else {
                showAlert(data.error || 'Gagal mendaftar', 'alert-danger');
                btnSubmit.disabled = false;
                btnSubmit.textContent = 'Daftar Sekarang';
            }
        } catch (error) {
            console.error('Error during registration:', error);
            showAlert('Gagal terhubung ke server', 'alert-danger');
            btnSubmit.disabled = false;
            btnSubmit.textContent = 'Daftar Sekarang';
        }
    });

    function showAlert(msg, className) {
        alertBox.textContent = msg;
        alertBox.style.display = 'block';
        alertBox.className = 'alert ' + className;
    }
});
