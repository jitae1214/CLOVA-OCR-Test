/**
 * 메뉴 분석 페이지 JavaScript
 * 
 * 파일 업로드, 드래그 앤 드롭, 복사 기능 등을 담당합니다.
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeUploadArea();
    initializeFileInput();
});

/**
 * 업로드 영역 초기화
 */
function initializeUploadArea() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('imageFile');
    const fileInfo = document.getElementById('fileInfo');

    if (!uploadArea || !fileInput || !fileInfo) return;

    // 클릭 이벤트
    uploadArea.addEventListener('click', function() {
        fileInput.click();
    });

    // 드래그 앤 드롭 이벤트
    uploadArea.addEventListener('dragover', function(e) {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', function(e) {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', function(e) {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFileSelect(files[0]);
        }
    });
}

/**
 * 파일 입력 초기화
 */
function initializeFileInput() {
    const fileInput = document.getElementById('imageFile');
    const analyzeBtn = document.querySelector('.analyze-btn');

    if (!fileInput) return;

    fileInput.addEventListener('change', function(e) {
        if (e.target.files.length > 0) {
            handleFileSelect(e.target.files[0]);
        }
    });

    // 폼 제출 시 로딩 상태
    const form = document.querySelector('.upload-form');
    if (form && analyzeBtn) {
        form.addEventListener('submit', function() {
            const btnText = analyzeBtn.querySelector('.btn-text');
            const btnLoading = analyzeBtn.querySelector('.btn-loading');
            
            if (btnText && btnLoading) {
                btnText.style.display = 'none';
                btnLoading.style.display = 'inline';
                analyzeBtn.disabled = true;
            }
        });
    }
}

/**
 * 파일 선택 처리
 */
function handleFileSelect(file) {
    const fileInput = document.getElementById('imageFile');
    const fileName = document.getElementById('fileName');
    const fileSize = document.getElementById('fileSize');
    const fileInfo = document.getElementById('fileInfo');

    if (!fileInput || !fileName || !fileSize || !fileInfo) return;

    // 파일 유효성 검사
    if (!file.type.startsWith('image/')) {
        showToast('이미지 파일만 업로드 가능합니다.', 'error');
        return;
    }

    // 파일 크기 검사 (10MB)
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
        showToast('파일 크기는 10MB를 초과할 수 없습니다.', 'error');
        return;
    }

    // 파일 정보 표시
    fileName.textContent = file.name;
    fileSize.textContent = formatFileSize(file.size);
    fileInfo.style.display = 'block';

    // 파일 입력에 파일 설정
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(file);
    fileInput.files = dataTransfer.files;

    showToast('파일이 선택되었습니다. 분석을 시작하세요!', 'success');
}

/**
 * 파일 크기 포맷팅
 */
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}

/**
 * 메뉴 항목 복사
 */
function copyToClipboard(button) {
    const menuItem = button.closest('.menu-item');
    const menuName = menuItem.querySelector('.menu-name');
    
    if (!menuName) return;

    const text = menuName.textContent.trim();
    
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(text).then(() => {
            showToast(`"${text}" 복사되었습니다!`, 'success');
        }).catch(() => {
            fallbackCopyTextToClipboard(text);
        });
    } else {
        fallbackCopyTextToClipboard(text);
    }
}

/**
 * 전체 메뉴 복사
 */
function copyAllMenus() {
    const menuItems = document.querySelectorAll('.menu-name');
    const menuList = Array.from(menuItems).map(item => item.textContent.trim());
    
    if (menuList.length === 0) {
        showToast('복사할 메뉴가 없습니다.', 'error');
        return;
    }

    const text = menuList.join('\n');
    
    if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard.writeText(text).then(() => {
            showToast(`${menuList.length}개 메뉴가 복사되었습니다!`, 'success');
        }).catch(() => {
            fallbackCopyTextToClipboard(text);
        });
    } else {
        fallbackCopyTextToClipboard(text);
    }
}

/**
 * 클립보드 복사 폴백 함수
 */
function fallbackCopyTextToClipboard(text) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    try {
        const successful = document.execCommand('copy');
        if (successful) {
            showToast('복사되었습니다!', 'success');
        } else {
            showToast('복사에 실패했습니다.', 'error');
        }
    } catch (err) {
        showToast('복사에 실패했습니다.', 'error');
    }
    
    document.body.removeChild(textArea);
}

/**
 * 메뉴 목록 다운로드
 */
function downloadMenuList() {
    const menuItems = document.querySelectorAll('.menu-name');
    const menuList = Array.from(menuItems).map(item => item.textContent.trim());
    
    if (menuList.length === 0) {
        showToast('다운로드할 메뉴가 없습니다.', 'error');
        return;
    }

    const text = menuList.join('\n');
    const blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    
    const link = document.createElement('a');
    link.href = url;
    link.download = 'menu-list.txt';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    URL.revokeObjectURL(url);
    showToast('메뉴 목록이 다운로드되었습니다!', 'success');
}

/**
 * 원본 텍스트 토글
 */
function toggleOriginalText() {
    const toggleBtn = document.querySelector('.toggle-btn');
    const originalText = document.getElementById('originalText');
    const toggleText = toggleBtn.querySelector('.toggle-text');
    const toggleIcon = toggleBtn.querySelector('.toggle-icon');
    
    if (!originalText || !toggleBtn) return;

    const isVisible = originalText.style.display !== 'none';
    
    if (isVisible) {
        originalText.style.display = 'none';
        toggleText.textContent = '📄 원본 텍스트 보기';
        toggleIcon.textContent = '▼';
        toggleBtn.classList.remove('active');
    } else {
        originalText.style.display = 'block';
        toggleText.textContent = '📄 원본 텍스트 숨기기';
        toggleIcon.textContent = '▲';
        toggleBtn.classList.add('active');
    }
}

/**
 * 토스트 메시지 표시
 */
function showToast(message, type = 'success') {
    // 기존 토스트 제거
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
        existingToast.remove();
    }

    // 새 토스트 생성
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    
    document.body.appendChild(toast);
    
    // 애니메이션 표시
    setTimeout(() => {
        toast.classList.add('show');
    }, 100);
    
    // 3초 후 자동 제거
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 3000);
}

/**
 * 페이지 로드 시 애니메이션
 */
function animateOnLoad() {
    const elements = document.querySelectorAll('.menu-item');
    elements.forEach((element, index) => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            element.style.transition = 'all 0.5s ease';
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, index * 100);
    });
}

// 페이지 로드 시 애니메이션 실행
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', animateOnLoad);
} else {
    animateOnLoad();
}

/**
 * 키보드 단축키
 */
document.addEventListener('keydown', function(e) {
    // Ctrl/Cmd + C로 전체 복사
    if ((e.ctrlKey || e.metaKey) && e.key === 'c' && e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
        const copyBtn = document.querySelector('.copy-all-btn');
        if (copyBtn && copyBtn.offsetParent !== null) {
            e.preventDefault();
            copyAllMenus();
        }
    }
    
    // Ctrl/Cmd + D로 다운로드
    if ((e.ctrlKey || e.metaKey) && e.key === 'd' && e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
        const downloadBtn = document.querySelector('.download-btn');
        if (downloadBtn && downloadBtn.offsetParent !== null) {
            e.preventDefault();
            downloadMenuList();
        }
    }
    
    // Escape로 원본 텍스트 토글
    if (e.key === 'Escape') {
        const originalText = document.getElementById('originalText');
        if (originalText && originalText.style.display !== 'none') {
            toggleOriginalText();
        }
    }
});
