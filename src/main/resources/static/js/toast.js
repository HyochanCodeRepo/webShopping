/**
 * Toast 알림 시스템
 */

// Toast 컨테이너 생성
function createToastContainer() {
    if (!document.getElementById('toast-container')) {
        const container = document.createElement('div');
        container.id = 'toast-container';
        container.style.cssText = `
            position: fixed;
            top: 80px;
            right: 20px;
            z-index: 10000;
            display: flex;
            flex-direction: column;
            gap: 10px;
        `;
        document.body.appendChild(container);
    }
}

/**
 * Toast 알림 표시
 * @param {string} message - 표시할 메시지
 * @param {string} type - 알림 타입 (success, error, warning, info)
 * @param {number} duration - 표시 시간 (ms)
 */
function showToast(message, type = 'info', duration = 3000) {
    createToastContainer();
    
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    
    // 타입별 아이콘 & 색상
    const typeConfig = {
        success: { icon: 'fa-check-circle', color: '#10b981', bgColor: '#d1fae5' },
        error: { icon: 'fa-times-circle', color: '#ef4444', bgColor: '#fee2e2' },
        warning: { icon: 'fa-exclamation-triangle', color: '#f59e0b', bgColor: '#fef3c7' },
        info: { icon: 'fa-info-circle', color: '#3b82f6', bgColor: '#dbeafe' }
    };
    
    const config = typeConfig[type] || typeConfig.info;
    
    toast.className = 'toast-item';
    toast.style.cssText = `
        min-width: 300px;
        max-width: 500px;
        padding: 16px 20px;
        background: ${config.bgColor};
        border-left: 4px solid ${config.color};
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        display: flex;
        align-items: center;
        gap: 12px;
        animation: slideIn 0.3s ease-out;
        cursor: pointer;
    `;
    
    toast.innerHTML = `
        <i class="fas ${config.icon}" style="color: ${config.color}; font-size: 20px;"></i>
        <span style="flex: 1; color: #333; font-size: 14px; font-weight: 500;">${message}</span>
        <i class="fas fa-times" style="color: #999; font-size: 14px; cursor: pointer;"></i>
    `;
    
    // 애니메이션 CSS 추가
    if (!document.getElementById('toast-animations')) {
        const style = document.createElement('style');
        style.id = 'toast-animations';
        style.textContent = `
            @keyframes slideIn {
                from {
                    transform: translateX(400px);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
            @keyframes slideOut {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(400px);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    }
    
    // 클릭 시 닫기
    toast.addEventListener('click', () => {
        removeToast(toast);
    });
    
    container.appendChild(toast);
    
    // 자동 제거
    if (duration > 0) {
        setTimeout(() => {
            removeToast(toast);
        }, duration);
    }
}

/**
 * Toast 제거
 */
function removeToast(toast) {
    toast.style.animation = 'slideOut 0.3s ease-out';
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 300);
}

/**
 * 성공 알림
 */
function showSuccess(message, duration = 3000) {
    showToast(message, 'success', duration);
}

/**
 * 에러 알림
 */
function showError(message, duration = 4000) {
    showToast(message, 'error', duration);
}

/**
 * 경고 알림
 */
function showWarning(message, duration = 3500) {
    showToast(message, 'warning', duration);
}

/**
 * 정보 알림
 */
function showInfo(message, duration = 3000) {
    showToast(message, 'info', duration);
}

// 페이지 로드 시 서버에서 전달된 메시지 표시
document.addEventListener('DOMContentLoaded', function() {
    // Thymeleaf에서 전달된 메시지 처리는 각 페이지에서 처리
    // 예: <script th:inline="javascript">
    //     const message = /*[[${message}]]*/ null;
    //     if (message) showSuccess(message);
    // </script>
});
