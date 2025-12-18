// ========================================
// Common JavaScript
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    
    // 맨 위로 이동
    const backToTop = document.querySelector('.back-to-top');
    if (backToTop) {
        backToTop.addEventListener('click', function(e) {
            e.preventDefault();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }
    
    // 검색 기능
    const searchBtn = document.querySelector('.search-btn');
    const searchInput = document.querySelector('.search-input');
    
    if (searchBtn && searchInput) {
        searchBtn.addEventListener('click', function() {
            performSearch();
        });
        
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
    }
    
    function performSearch() {
        const keyword = searchInput.value.trim();
        const category = document.querySelector('.search-category').value;
        
        if (keyword) {
            let url = '/products?keyword=' + encodeURIComponent(keyword);
            if (category !== 'all') {
                url += '&category=' + encodeURIComponent(category);
            }
            window.location.href = url;
        }
    }
    
    // 장바구니 수량 업데이트 (나중에 구현)
    function updateCartCount(count) {
        const cartCount = document.querySelector('.cart-count');
        if (cartCount) {
            cartCount.textContent = count;
        }
    }
    
});

// ========================================
// Utility Functions
// ========================================

// 가격 포맷팅
function formatPrice(price) {
    return new Intl.NumberFormat('ko-KR').format(price);
}

// 알림 메시지
function showAlert(message, type = 'info') {
    // 간단한 알림 (나중에 더 예쁘게 구현 가능)
    alert(message);
}

// 확인 다이얼로그
function showConfirm(message) {
    return confirm(message);
}
