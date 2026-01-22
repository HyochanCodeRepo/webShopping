// ========================================
// Common JavaScript
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    
    // 페이지 로드 시 장바구니 개수 가져오기
    updateCartCount();
    
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
            let url = '/product/search?keyword=' + encodeURIComponent(keyword);
            if (category && category !== 'all' && category !== '') {
                url += '&categoryId=' + category;
            }
            window.location.href = url;
        }
    }
    
});

// ========================================
// 장바구니 관련 함수
// ========================================

/**
 * 장바구니 개수 업데이트
 */
async function updateCartCount() {
    try {
        const response = await fetch('/cart/count');
        
        if (response.ok) {
            const data = await response.json();
            const cartCountElement = document.querySelector('.cart-count');
            
            if (cartCountElement) {
                const count = data.count || 0;
                cartCountElement.textContent = count;
                
                // 장바구니가 비어있으면 뱃지 숨기기
                if (count === 0) {
                    cartCountElement.classList.add('empty');
                } else {
                    cartCountElement.classList.remove('empty');
                }
            }
        }
    } catch (error) {
        console.error('장바구니 개수 조회 실패:', error);
        // 에러가 나도 UI는 유지 (조용히 실패)
    }
}

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
