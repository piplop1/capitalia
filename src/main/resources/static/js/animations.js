document.addEventListener('DOMContentLoaded', () => {

    // --- 1. Animación de "Revelado al Scroll" ---
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                // Una vez visible, no necesitamos seguir observándolo
                observer.unobserve(entry.target); 
            }
        });
    }, {
        threshold: 0.1 // El elemento se considera visible cuando el 10% está en pantalla
    });

    // Observar todos los elementos que queremos animar al hacer scroll
    const elementsToAnimate = document.querySelectorAll('.about-card, .team-member, .results-card, .feature-card, .call-to-action, .service-card');
    elementsToAnimate.forEach(el => {
        el.classList.add('hidden-on-load'); // Añadir clase para estado inicial
        observer.observe(el);
    });


    // --- 2. Contadores Dinámicos de Estadísticas ---
    const statsObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const counters = entry.target.querySelectorAll('.stat-number');
                counters.forEach(counter => {
                    const target = +counter.getAttribute('data-target');
                    const duration = 2000; // 2 segundos
                    const stepTime = Math.abs(Math.floor(duration / target));
                    let current = 0;

                    const timer = setInterval(() => {
                        current += 1;
                        counter.innerText = current.toLocaleString(); // Formatea números grandes con comas
                        if (current === target) {
                            clearInterval(timer);
                        }
                    }, stepTime < 1 ? 1 : stepTime); // Evita que el intervalo sea 0
                });
                // Dejar de observar una vez que la animación ha comenzado
                observer.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.5
    });

    const statsContainer = document.querySelector('.stats-container');
    if (statsContainer) {
        statsObserver.observe(statsContainer);
    }


    // --- 3. Interacción en Tarjeta de Equipo (Hover para Rol) ---
    const teamMembers = document.querySelectorAll('.team-member');

    teamMembers.forEach(member => {
        const role = member.getAttribute('data-role');
        if (role) {
            // Crear el elemento para el tooltip
            const tooltip = document.createElement('div');
            tooltip.className = 'member-role-tooltip';
            tooltip.innerText = role;
            member.appendChild(tooltip);

            // Mostrar y ocultar con el cursor
            member.addEventListener('mouseenter', () => {
                tooltip.style.display = 'block';
                setTimeout(() => tooltip.style.opacity = '1', 10); // Pequeño delay para la transición
            });

            member.addEventListener('mouseleave', () => {
                tooltip.style.opacity = '0';
                setTimeout(() => tooltip.style.display = 'none', 300); // Esperar a que termine la transición
            });
        }
    });

    // --- 4. Carrusel de Tarjetas 3D ---
    const carouselContainer = document.querySelector('.carousel-container');
    const carousel = document.querySelector('.card-carousel');

    if (carousel && carouselContainer) {
        const cards = document.querySelectorAll('.service-card');
        const numCards = cards.length;
        const angle = 360 / numCards;
        let currentAngle = 0;
        let autoRotateInterval;

        const rotateNext = () => {
            currentAngle -= angle;
            carousel.style.transform = `rotateY(${currentAngle}deg)`;
        };

        const startAutoRotate = () => {
            clearInterval(autoRotateInterval); // Previene intervalos duplicados
            autoRotateInterval = setInterval(rotateNext, 5000);
        };

        // Pausa la rotación al pasar el cursor y la reanuda al quitarlo
        carouselContainer.addEventListener('mouseenter', () => clearInterval(autoRotateInterval));
        carouselContainer.addEventListener('mouseleave', startAutoRotate);

        startAutoRotate(); // Inicia la rotación automática
    }

});