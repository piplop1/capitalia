document.addEventListener('DOMContentLoaded', function () {

    // --- LÓGICA DE NAVEGACIÓN (MENÚ HAMBURGUESA) ---
    const hamburgerButton = document.querySelector('.hamburger-button');
    const navLinks = document.querySelector('.nav-links');
    if (hamburgerButton && navLinks) {
        hamburgerButton.addEventListener('click', () => {
            navLinks.classList.toggle('active');
        });
    }

    // --- LÓGICA PARA MODALES ---
    const openModalButtons = document.querySelectorAll('[data-modal-target]');
    const closeModalButtons = document.querySelectorAll('.modal-close-btn');
    const overlays = document.querySelectorAll('.modal-overlay');

    openModalButtons.forEach(button => {
        button.addEventListener('click', () => {
            const modal = document.querySelector(button.dataset.modalTarget);
            openModal(modal);

            // Lógica específica para los modales de gestión de usuarios
            if (modal.id === 'edit-user-modal') {
                // Rellenar el formulario de edición
                document.getElementById('edit-id').value = button.dataset.id;
                document.getElementById('edit-fullname').value = button.dataset.fullname;
                document.getElementById('edit-email').value = button.dataset.email;
                document.getElementById('edit-role').value = button.dataset.role;
            } else if (modal.id === 'delete-confirm-modal') {
                // Configurar la acción del formulario de eliminación
                const userId = button.dataset.id;
                const deleteForm = document.getElementById('delete-user-form');
                if (deleteForm) {
                    deleteForm.action = `/usuarios/eliminar/${userId}`;
                }
            }
        });
    });

    overlays.forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                closeModal(overlay);
            }
        });
    });

    closeModalButtons.forEach(button => {
        button.addEventListener('click', () => {
            const modal = button.closest('.modal-overlay');
            closeModal(modal);
        });
    });

    function openModal(modal) {
        if (modal == null) return;
        modal.classList.add('active');
    }

    function closeModal(modal) {
        if (modal == null) return;
        modal.classList.remove('active');
    }

    // --- LÓGICA PARA RESTABLECER CONTRASEÑA ---
    const requestCodeForm = document.getElementById('form-request-code');
    const verifyCodeForm = document.getElementById('form-verify-code');
    const resetPasswordForm = document.getElementById('form-reset-password');
    const messageContainer = document.getElementById('reset-message-container');

    if (requestCodeForm) {
        requestCodeForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('reset-email').value;
            messageContainer.textContent = 'Enviando...';

            try {
                const response = await fetch('/password/request', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email: email })
                });

                const result = await response.json();

                if (response.ok) {
                    messageContainer.textContent = result.message;
                    messageContainer.style.color = 'green';
                    // Ocultar paso 1 y mostrar paso 2
                    document.getElementById('reset-step-1').style.display = 'none';
                    document.getElementById('reset-step-2').style.display = 'block';
                } else {
                    throw new Error(result.error || 'Ocurrió un error.');
                }
            } catch (error) {
                messageContainer.textContent = error.message;
                messageContainer.style.color = 'red';
            }
        });
    }

    if (verifyCodeForm) {
        verifyCodeForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('reset-email').value; // Tomamos el email del paso anterior
            const code = document.getElementById('verification-code').value;
            messageContainer.textContent = 'Verificando...';

            try {
                const response = await fetch('/password/verify', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email: email, code: code })
                });

                const result = await response.json();

                if (response.ok) {
                    messageContainer.textContent = result.message;
                    messageContainer.style.color = 'green';
                    // Ocultar paso 2 y mostrar paso 3
                    document.getElementById('reset-step-2').style.display = 'none';
                    document.getElementById('reset-step-3').style.display = 'block';
                } else {
                    throw new Error(result.error || 'Ocurrió un error en la verificación.');
                }
            } catch (error) {
                messageContainer.textContent = error.message;
                messageContainer.style.color = 'red';
            }
        });
    }

    if (resetPasswordForm) {
        resetPasswordForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('reset-email').value;
            const code = document.getElementById('verification-code').value;
            const newPassword = document.getElementById('new-password').value;
            const confirmPassword = document.getElementById('confirm-password').value;

            if (newPassword !== confirmPassword) {
                messageContainer.textContent = 'Las contraseñas no coinciden.';
                messageContainer.style.color = 'red';
                return;
            }

            messageContainer.textContent = 'Actualizando contraseña...';

            try {
                const response = await fetch('/password/reset', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, code, newPassword })
                });

                const result = await response.json();

                if (response.ok) {
                    messageContainer.textContent = result.message;
                    messageContainer.style.color = 'green';
                    // Ocultar el formulario final
                    document.getElementById('reset-step-3').style.display = 'none';
                    // Opcional: cerrar el modal después de unos segundos
                    setTimeout(() => closeModal(document.getElementById('password-reset-modal')), 3000);
                } else {
                    throw new Error(result.error || 'Ocurrió un error en la verificación.');
                }
            } catch (error) {
                messageContainer.textContent = error.message;
                messageContainer.style.color = 'red';
            }
        });
    }


    // La lógica de autenticación y gestión de usuarios ahora es manejada por el backend (Spring Boot).
});

document.addEventListener('DOMContentLoaded', function() {
    
    // --- LÓGICA PARA EL MODAL DE EDITAR ---
    const editButtons = document.querySelectorAll('.edit-icon');
    
    editButtons.forEach(button => {
        button.addEventListener('click', function() {
            // 1. Capturar los datos del botón
            const id = this.getAttribute('data-id');
            const fullname = this.getAttribute('data-fullname');
            const email = this.getAttribute('data-email');
            
            // 2. Pasarlos a los inputs del Modal
            // ¡ESTA ES LA LÍNEA QUE ARREGLA TU ERROR!
            document.getElementById('edit-id').value = id; 
            
            document.getElementById('edit-fullname').value = fullname;
            document.getElementById('edit-email').value = email;

            // 3. Mostrar el modal (Lógica básica si no usas librerías)
            const modalTarget = document.querySelector(this.getAttribute('data-modal-target'));
            if(modalTarget) modalTarget.style.display = 'flex';
        });
    });

    // --- LÓGICA PARA EL MODAL DE ELIMINAR ---
    const deleteButtons = document.querySelectorAll('.delete-icon');
    
    deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            
            // Actualizamos la acción del formulario dinámicamente
            // Para que envíe a: /admin/eliminar/5 (por ejemplo)
            const deleteForm = document.getElementById('delete-admin-form');
            deleteForm.action = '/admin/eliminar/' + id;

            const modalTarget = document.querySelector(this.getAttribute('data-modal-target'));
            if(modalTarget) modalTarget.style.display = 'flex';
        });
    });

    // --- LÓGICA PARA CERRAR MODALES (Botón X y Cancelar) ---
    const closeButtons = document.querySelectorAll('.modal-close-btn');
    closeButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const modal = this.closest('.modal-overlay');
            if(modal) modal.style.display = 'none';
        });
    });
});


document.addEventListener('DOMContentLoaded', () => {
    
    // Variables para almacenar datos entre pasos
    let userEmail = "";
    let userCode = "";

    // --- PASO 1: ENVIAR CORREO ---
    const formStep1 = document.getElementById('form-step-1');
    if (formStep1) {
        formStep1.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = formStep1.querySelector('button');
            const msg = document.getElementById('msg-step-1');
            const emailInput = document.getElementById('reset-email').value;

            btn.disabled = true;
            btn.textContent = "Enviando...";
            msg.textContent = "";

            try {
                const response = await fetch('/api/password/request', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email: emailInput })
                });

                if (response.ok) {
                    userEmail = emailInput; // Guardar email
                    // Cambiar de paso visualmente
                    document.getElementById('step-1-email').style.display = 'none';
                    document.getElementById('step-2-code').style.display = 'block';
                } else {
                    const data = await response.json();
                    msg.textContent = data.error || "Error al enviar correo.";
                    msg.style.color = "red";
                    btn.disabled = false;
                    btn.textContent = "Enviar Código";
                }
            } catch (error) {
                console.error(error);
                msg.textContent = "Error de conexión.";
                btn.disabled = false;
            }
        });
    }

    // --- PASO 2: VERIFICAR CÓDIGO ---
    const formStep2 = document.getElementById('form-step-2');
    if (formStep2) {
        formStep2.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = formStep2.querySelector('button');
            const msg = document.getElementById('msg-step-2');
            const codeInput = document.getElementById('reset-code').value;

            btn.disabled = true;
            btn.textContent = "Verificando...";

            try {
                const response = await fetch('/api/password/verify', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ code: codeInput })
                });

                if (response.ok) {
                    userCode = codeInput; // Guardar código para validación final
                    document.getElementById('step-2-code').style.display = 'none';
                    document.getElementById('step-3-password').style.display = 'block';
                } else {
                    const data = await response.json();
                    msg.textContent = data.error || "Código incorrecto.";
                    msg.style.color = "red";
                    btn.disabled = false;
                    btn.textContent = "Verificar";
                }
            } catch (error) {
                msg.textContent = "Error de conexión.";
                btn.disabled = false;
            }
        });
    }

    // --- PASO 3: CAMBIAR CONTRASEÑA ---
    const formStep3 = document.getElementById('form-step-3');
    if (formStep3) {
        formStep3.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = formStep3.querySelector('button');
            const passwordInput = document.getElementById('new-password').value;

            btn.disabled = true;
            btn.textContent = "Guardando...";

            try {
                const response = await fetch('/api/password/reset', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ 
                        email: userEmail, 
                        code: userCode, 
                        password: passwordInput 
                    })
                });

                if (response.ok) {
                    // Mostrar éxito y redirigir
                    document.getElementById('step-3-password').style.display = 'none';
                    document.getElementById('step-success').style.display = 'block';
                    
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 2000);
                } else {
                    alert("Error al actualizar contraseña.");
                    btn.disabled = false;
                }
            } catch (error) {
                console.error(error);
                btn.disabled = false;
            }
        });
    }
});