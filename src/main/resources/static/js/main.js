// ========================================
// Main Page JavaScript
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    initBannerSlider();
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
