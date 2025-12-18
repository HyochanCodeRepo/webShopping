// ========================================
// Members Page JavaScript
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    initPasswordValidation();
    initPhoneFormat();
});

// 비밀번호 확인 검증
function initPasswordValidation() {
    const password = document.getElementById('password');
    const passwordConfirm = document.getElementById('passwordConfirm');
    const form = document.querySelector('.auth-form');
    
    if (form && password && passwordConfirm) {
        form.addEventListener('submit', function(e) {
            if (password.value !== passwordConfirm.value) {
                e.preventDefault();
                alert('비밀번호가 일치하지 않습니다.');
                passwordConfirm.focus();
                return false;
            }
        });
        
        // 실시간 비밀번호 확인
        passwordConfirm.addEventListener('input', function() {
            if (this.value && password.value !== this.value) {
                this.style.borderColor = '#CC0C39';
            } else {
                this.style.borderColor = '#a6a6a6';
            }
        });
    }
}

// 전화번호 자동 포맷
function initPhoneFormat() {
    const phoneInput = document.getElementById('phone');
    
    if (phoneInput) {
        phoneInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/[^0-9]/g, '');
            
            if (value.length > 11) {
                value = value.slice(0, 11);
            }
            
            if (value.length > 7) {
                value = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7);
            } else if (value.length > 3) {
                value = value.slice(0, 3) + '-' + value.slice(3);
            }
            
            e.target.value = value;
        });
    }
}
