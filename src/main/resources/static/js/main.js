// ========================================
// Main Page JavaScript
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    initBannerSlider();
    initCarousels();
});

// 배너 슬라이더
function initBannerSlider() {
    const slides = document.querySelectorAll('.banner-slide');
    const prevBtn = document.querySelector('.banner-prev');
    const nextBtn = document.querySelector('.banner-next');
    
    if (slides.length === 0) return;
    
    let currentIndex = 0;
    let autoSlideInterval;
    
    function showSlide(index) {
        slides.forEach(slide => slide.classList.remove('active'));
        
        if (index >= slides.length) {
            currentIndex = 0;
        } else if (index < 0) {
            currentIndex = slides.length - 1;
        } else {
            currentIndex = index;
        }
        
        slides[currentIndex].classList.add('active');
    }
    
    function nextSlide() {
        showSlide(currentIndex + 1);
    }
    
    function prevSlide() {
        showSlide(currentIndex - 1);
    }
    
    function startAutoSlide() {
        autoSlideInterval = setInterval(nextSlide, 5000);
    }
    
    function stopAutoSlide() {
        clearInterval(autoSlideInterval);
    }
    
    // 이벤트 리스너
    if (prevBtn) {
        prevBtn.addEventListener('click', function() {
            stopAutoSlide();
            prevSlide();
            startAutoSlide();
        });
    }
    
    if (nextBtn) {
        nextBtn.addEventListener('click', function() {
            stopAutoSlide();
            nextSlide();
            startAutoSlide();
        });
    }
    
    // 자동 슬라이드 시작
    startAutoSlide();
    
    // 마우스 오버시 자동 슬라이드 정지
    const banner = document.querySelector('.hero-banner');
    if (banner) {
        banner.addEventListener('mouseenter', stopAutoSlide);
        banner.addEventListener('mouseleave', startAutoSlide);
    }
}

// 캐러셀 초기화
const carouselStates = {
    popular: { currentPage: 0 },
    new: { currentPage: 0 }
};

function initCarousels() {
    // 캐러셀 페이지 표시 업데이트
    updateCarouselDisplay('popular');
    updateCarouselDisplay('new');
}

// 캐러셀 이동
function moveCarousel(type, direction) {
    const carousel = document.getElementById(type + '-carousel');
    if (!carousel) return;
    
    const cards = carousel.querySelectorAll('.product-card');
    const totalCards = cards.length;
    const cardsPerPage = 4; // 한 번에 보여줄 카드 수
    const maxPage = Math.ceil(totalCards / cardsPerPage) - 1;
    
    // 현재 페이지 업데이트
    carouselStates[type].currentPage += direction;
    
    // 경계 체크
    if (carouselStates[type].currentPage < 0) {
        carouselStates[type].currentPage = maxPage;
    } else if (carouselStates[type].currentPage > maxPage) {
        carouselStates[type].currentPage = 0;
    }
    
    // 표시 업데이트
    updateCarouselDisplay(type);
}

// 캐러셀 표시 업데이트
function updateCarouselDisplay(type) {
    const carousel = document.getElementById(type + '-carousel');
    if (!carousel) return;
    
    const cards = carousel.querySelectorAll('.product-card');
    const totalCards = cards.length;
    const cardsPerPage = 4;
    const currentPage = carouselStates[type].currentPage;
    
    // 모든 카드 숨김
    cards.forEach((card, index) => {
        const startIndex = currentPage * cardsPerPage;
        const endIndex = startIndex + cardsPerPage;
        
        if (index >= startIndex && index < endIndex) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}
