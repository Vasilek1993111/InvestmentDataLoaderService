// Ждем загрузки DOM
document.addEventListener('DOMContentLoaded', function() {
    
    // Модальные окна
    const modalLogin = document.querySelector('.modal-login');
    const modalMap = document.querySelector('.modal-map');
    const loginLink = document.querySelector('.login-link');
    const mapLinks = document.querySelectorAll('a[href="#map"]');
    const modalCloseButtons = document.querySelectorAll('.modal-close');
    
    // Открытие модального окна входа
    if (loginLink) {
        loginLink.addEventListener('click', function(e) {
            e.preventDefault();
            modalLogin.classList.add('show');
            document.body.style.overflow = 'hidden';
        });
    }
    
    // Открытие модального окна карты
    mapLinks.forEach(function(link) {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            modalMap.classList.add('show');
            document.body.style.overflow = 'hidden';
        });
    });
    
    // Закрытие модальных окон
    modalCloseButtons.forEach(function(button) {
        button.addEventListener('click', function() {
            modalLogin.classList.remove('show');
            modalMap.classList.remove('show');
            document.body.style.overflow = '';
        });
    });
    
    // Закрытие по клику вне модального окна
    [modalLogin, modalMap].forEach(function(modal) {
        if (modal) {
            modal.addEventListener('click', function(e) {
                if (e.target === modal) {
                    modal.classList.remove('show');
                    document.body.style.overflow = '';
                }
            });
        }
    });
    
    // Закрытие по Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            modalLogin.classList.remove('show');
            modalMap.classList.remove('show');
            document.body.style.overflow = '';
        }
    });
    
    // Галерея
    const galleryImages = [
        'img/studio.jpg',
        'img/studio-2.jpg',
        'img/studio-3.jpg'
    ];
    
    let currentImageIndex = 0;
    const galleryContent = document.querySelector('.gallery-content img');
    const galleryBackButton = document.querySelector('.gallery-button-back');
    const galleryNextButton = document.querySelector('.gallery-button-next');
    
    if (galleryContent && galleryBackButton && galleryNextButton) {
        // Функция для обновления изображения
        function updateGalleryImage() {
            galleryContent.src = galleryImages[currentImageIndex];
            galleryContent.alt = `Интерьер ${currentImageIndex + 1}`;
        }
        
        // Кнопка "Назад"
        galleryBackButton.addEventListener('click', function() {
            currentImageIndex = (currentImageIndex - 1 + galleryImages.length) % galleryImages.length;
            updateGalleryImage();
        });
        
        // Кнопка "Вперед"
        galleryNextButton.addEventListener('click', function() {
            currentImageIndex = (currentImageIndex + 1) % galleryImages.length;
            updateGalleryImage();
        });
    }
    
    // Форма записи
    const appointmentForm = document.querySelector('.appointment-form');
    if (appointmentForm) {
        appointmentForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Получаем данные формы
            const formData = new FormData(appointmentForm);
            const date = formData.get('date');
            const time = formData.get('time');
            const name = formData.get('name');
            const phone = formData.get('phone');
            
            // Простая валидация
            if (!date || !time || !name || !phone) {
                alert('Пожалуйста, заполните все поля');
                return;
            }
            
            // Проверка формата даты
            const dateRegex = /^\d{2}\.\d{2}\.\d{4}$/;
            if (!dateRegex.test(date)) {
                alert('Пожалуйста, введите дату в формате ДД.ММ.ГГГГ');
                return;
            }
            
            // Проверка формата времени
            const timeRegex = /^\d{2}:\d{2}$/;
            if (!timeRegex.test(time)) {
                alert('Пожалуйста, введите время в формате ЧЧ:ММ');
                return;
            }
            
            // Проверка телефона
            const phoneRegex = /^\+7\s\d{3}\s\d{3}-\d{2}-\d{2}$/;
            if (!phoneRegex.test(phone)) {
                alert('Пожалуйста, введите телефон в формате +7 XXX XXX-XX-XX');
                return;
            }
            
            // Имитация отправки
            alert('Спасибо за запись! Мы свяжемся с вами для подтверждения.');
            appointmentForm.reset();
        });
    }
    
    // Форма входа
    const loginForm = document.querySelector('.login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(loginForm);
            const login = formData.get('login');
            const password = formData.get('password');
            
            if (!login || !password) {
                alert('Пожалуйста, введите логин и пароль');
                return;
            }
            
            // Простая проверка email
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(login)) {
                alert('Пожалуйста, введите корректный email');
                return;
            }
            
            if (password.length < 6) {
                alert('Пароль должен содержать минимум 6 символов');
                return;
            }
            
            alert('Вход выполнен успешно!');
            modalLogin.classList.remove('show');
            document.body.style.overflow = '';
            loginForm.reset();
        });
    }
    
    // Плавная прокрутка для якорных ссылок
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    anchorLinks.forEach(function(link) {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            
            // Пропускаем модальные окна
            if (href === '#login' || href === '#map') {
                return;
            }
            
            e.preventDefault();
            const target = document.querySelector(href);
            
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Анимация появления элементов при скролле
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(function(entry) {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);
    
    // Наблюдаем за элементами для анимации
    const animatedElements = document.querySelectorAll('.feature-item, .news, .gallery, .contacts, .appointment');
    animatedElements.forEach(function(element) {
        element.style.opacity = '0';
        element.style.transform = 'translateY(30px)';
        element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(element);
    });
    
    // Маска для телефона
    const phoneInput = document.getElementById('appointment-phone');
    if (phoneInput) {
        phoneInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            
            if (value.length === 0) {
                e.target.value = '';
                return;
            }
            
            if (value.length === 1 && value[0] === '7') {
                value = value.substring(1);
            }
            
            if (value.length === 0) {
                e.target.value = '';
                return;
            }
            
            if (value.length <= 3) {
                e.target.value = '+7 ' + value;
            } else if (value.length <= 6) {
                e.target.value = '+7 ' + value.substring(0, 3) + ' ' + value.substring(3);
            } else if (value.length <= 8) {
                e.target.value = '+7 ' + value.substring(0, 3) + ' ' + value.substring(3, 6) + '-' + value.substring(6);
            } else {
                e.target.value = '+7 ' + value.substring(0, 3) + ' ' + value.substring(3, 6) + '-' + value.substring(6, 8) + '-' + value.substring(8, 10);
            }
        });
    }
    
    // Маска для даты
    const dateInput = document.getElementById('appointment-date');
    if (dateInput) {
        dateInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            
            if (value.length <= 2) {
                e.target.value = value;
            } else if (value.length <= 4) {
                e.target.value = value.substring(0, 2) + '.' + value.substring(2);
            } else {
                e.target.value = value.substring(0, 2) + '.' + value.substring(2, 4) + '.' + value.substring(4, 8);
            }
        });
    }
    
    // Маска для времени
    const timeInput = document.getElementById('appointment-time');
    if (timeInput) {
        timeInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            
            if (value.length <= 2) {
                e.target.value = value;
            } else {
                e.target.value = value.substring(0, 2) + ':' + value.substring(2, 4);
            }
        });
    }
    
    // Добавляем текущую дату в поле даты
    if (dateInput) {
        const today = new Date();
        const day = String(today.getDate()).padStart(2, '0');
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const year = today.getFullYear();
        dateInput.placeholder = `${day}.${month}.${year}`;
    }
    
    // Добавляем текущее время в поле времени
    if (timeInput) {
        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        timeInput.placeholder = `${hours}:${minutes}`;
    }
    
    // Эффект параллакса для фона
    window.addEventListener('scroll', function() {
        const scrolled = window.pageYOffset;
        const parallaxElements = document.querySelectorAll('.feature-item');
        
        parallaxElements.forEach(function(element, index) {
            const speed = 0.5 + (index * 0.1);
            const yPos = -(scrolled * speed);
            element.style.transform = `translateY(${yPos}px)`;
        });
    });
    
    // Добавляем индикатор загрузки
    window.addEventListener('load', function() {
        const loader = document.createElement('div');
        loader.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: #000;
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 9999;
            transition: opacity 0.5s ease;
        `;
        loader.innerHTML = '<div style="color: white; font-size: 24px;">Загрузка...</div>';
        
        document.body.appendChild(loader);
        
        setTimeout(function() {
            loader.style.opacity = '0';
            setTimeout(function() {
                document.body.removeChild(loader);
            }, 500);
        }, 1000);
    });
});