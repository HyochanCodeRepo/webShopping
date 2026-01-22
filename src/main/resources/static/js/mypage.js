/**
 * 마이페이지 JavaScript
 */

// 비밀번호 확인 유효성 검사
document.addEventListener('DOMContentLoaded', function() {
    const passwordForm = document.querySelector('form[action*="change-password"]');
    
    if (passwordForm) {
        passwordForm.addEventListener('submit', function(e) {
            const newPassword = document.getElementById('newPassword').value;
            const newPasswordConfirm = document.getElementById('newPasswordConfirm').value;
            
            if (newPassword !== newPasswordConfirm) {
                e.preventDefault();
                alert('새 비밀번호가 일치하지 않습니다.');
                return false;
            }
            
            if (newPassword.length < 4) {
                e.preventDefault();
                alert('비밀번호는 4자 이상이어야 합니다.');
                return false;
            }
        });
    }
    
    // 기본 정보 수정 폼 유효성 검사
    const profileForm = document.querySelector('form[action*="mypage/update"]');
    
    if (profileForm) {
        profileForm.addEventListener('submit', function(e) {
            const name = document.getElementById('name').value.trim();
            
            if (name === '') {
                e.preventDefault();
                alert('이름을 입력해주세요.');
                return false;
            }
        });
    }
});
