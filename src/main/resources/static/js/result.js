/**
 * OCR 결과 페이지 JavaScript
 * 이미지 하이라이트, 텍스트 복사, 키보드 네비게이션 등의 기능 제공
 */

/* ========================================
   전역 변수 선언
   ======================================== */

// 서버에서 전달받은 텍스트 박스 좌표 정보 (HTML에서 전역 변수로 설정됨)
let textBoxes = [];

// 현재 하이라이트된 텍스트 인덱스 (-1: 선택 없음)
let currentHighlightIndex = -1;

/* ========================================
   텍스트 복사 기능
   ======================================== */

/**
 * 개별 텍스트 복사 기능
 * 버튼의 data-text 속성에서 텍스트를 가져와 복사
 * @param {HTMLElement} button - 클릭된 복사 버튼
 */
function copyTextFromData(button) {
    const text = button.getAttribute('data-text');
    copyText(text, button);
}

/**
 * 텍스트를 클립보드에 복사하고 시각적 피드백 제공
 * @param {string} text - 복사할 텍스트
 * @param {HTMLElement} button - 피드백을 표시할 버튼
 */
function copyText(text, button) {
    navigator.clipboard.writeText(text).then(function() {
        // 복사 성공 시 시각적 피드백
        const icon = button.querySelector('i');
        const originalClass = icon.className;
        
        // 복사 완료 표시 (체크 아이콘으로 변경)
        icon.className = 'fas fa-check';
        button.classList.add('copied');
        
        // 2초 후 원래 상태로 복원
        setTimeout(function() {
            icon.className = originalClass;
            button.classList.remove('copied');
        }, 2000);
    }).catch(function(err) {
        // 복사 실패 시 알림
        alert('복사에 실패했습니다: ' + err);
    });
}

/**
 * 전체 텍스트 복사 기능
 * 모든 추출된 텍스트를 하나로 합쳐서 복사
 */
function copyAllText() {
    const allText = document.getElementById('allText').textContent;
    navigator.clipboard.writeText(allText).then(function() {
        // 성공 시 버튼 상태 변경
        const btn = event.target.closest('button');
        const originalText = btn.innerHTML;
        btn.innerHTML = '<i class="fas fa-check me-1"></i>복사됨';
        btn.classList.add('btn-success');
        btn.classList.remove('btn-outline-success');
        
        // 2초 후 원래 상태로 복원
        setTimeout(function() {
            btn.innerHTML = originalText;
            btn.classList.remove('btn-success');
            btn.classList.add('btn-outline-success');
        }, 2000);
    }).catch(function(err) {
        alert('복사에 실패했습니다: ' + err);
    });
}

/* ========================================
   텍스트 하이라이트 기능
   ======================================== */

/**
 * 개별 텍스트 클릭 시 하이라이트 처리
 * @param {HTMLElement} element - 클릭된 텍스트 아이템
 */
function highlightTextBox(element) {
    const index = parseInt(element.getAttribute('data-index'));
    console.log('highlightTextBox called with index:', index);
    console.log('textBoxes available:', textBoxes.length);
    
    if (textBoxes.length === 0) {
        console.warn('textBoxes 데이터가 없습니다. 하이라이트를 표시할 수 없습니다.');
        return;
    }
    
    // 이전 선택 해제 (모든 텍스트 아이템 초기화)
    document.querySelectorAll('.text-item').forEach(item => {
        item.style.backgroundColor = 'transparent';
        item.style.borderLeft = 'none';
        item.style.color = '';
    });
    
    // 현재 선택 표시 (유튜브 스타일 하이라이트)
    element.style.backgroundColor = 'var(--youtube-surface-variant)';
    element.style.borderLeft = '4px solid var(--youtube-accent)';
    
    // 텍스트 색상 유지
    const textContent = element.querySelector('.text-content');
    if (textContent) {
        textContent.style.color = 'var(--youtube-text-primary)';
    }
    
    // 해당 텍스트 박스 하이라이트 표시
    showSingleHighlight(index);
    currentHighlightIndex = index;
}

/**
 * 단일 텍스트 박스 하이라이트 표시
 * @param {number} index - 하이라이트할 텍스트 박스 인덱스
 */
function showSingleHighlight(index) {
    const img = document.getElementById('uploadedImage');
    const overlay = document.getElementById('highlightOverlay');
    
    // 유효성 검사
    if (!img || !textBoxes || index >= textBoxes.length) return;
    
    // 이미지가 완전히 로드된 후 실행
    if (img.complete) {
        drawSingleHighlight(index);
    } else {
        img.onload = () => drawSingleHighlight(index);
    }
}

/**
 * 실제 하이라이트 박스를 그리는 함수
 * OCR 좌표를 화면 좌표로 변환하여 하이라이트 박스 생성
 * @param {number} index - 하이라이트할 텍스트 박스 인덱스
 */
function drawSingleHighlight(index) {
    const img = document.getElementById('uploadedImage');
    const overlay = document.getElementById('highlightOverlay');
    const textBox = textBoxes[index];
    
    // 유효성 검사
    if (!textBox || !textBox.vertices || textBox.vertices.length < 4) return;
    
    // 이미지가 완전히 로드되었는지 확인
    if (!img.complete || img.naturalWidth === 0 || img.naturalHeight === 0) {
        setTimeout(() => drawSingleHighlight(index), 100);
        return;
    }
    
    // 이미지의 실제 표시 크기 계산 (object-fit: contain 고려)
    const imgNaturalRatio = img.naturalWidth / img.naturalHeight;
    const imgDisplayRatio = img.clientWidth / img.clientHeight;
    
    let actualImageWidth, actualImageHeight, imageOffsetX, imageOffsetY;
    
    if (imgNaturalRatio > imgDisplayRatio) {
        // 이미지가 너비에 맞춰짐 (위아래 여백 발생)
        actualImageWidth = img.clientWidth;
        actualImageHeight = img.clientWidth / imgNaturalRatio;
        imageOffsetX = 0;
        imageOffsetY = (img.clientHeight - actualImageHeight) / 2;
    } else {
        // 이미지가 높이에 맞춰짐 (좌우 여백 발생)
        actualImageWidth = img.clientHeight * imgNaturalRatio;
        actualImageHeight = img.clientHeight;
        imageOffsetX = (img.clientWidth - actualImageWidth) / 2;
        imageOffsetY = 0;
    }
    
    // 스케일 비율 계산 (원본 이미지 크기 -> 표시 크기)
    const scaleX = actualImageWidth / img.naturalWidth;
    const scaleY = actualImageHeight / img.naturalHeight;
    
    // 오버레이 설정
    overlay.style.width = img.clientWidth + 'px';
    overlay.style.height = img.clientHeight + 'px';
    overlay.innerHTML = '';  // 기존 하이라이트 제거
    
    const vertices = textBox.vertices;
    
    // OCR 좌표를 화면 좌표로 변환
    const minX = Math.min(...vertices.map(v => v.x)) * scaleX + imageOffsetX;
    const minY = Math.min(...vertices.map(v => v.y)) * scaleY + imageOffsetY;
    const maxX = Math.max(...vertices.map(v => v.x)) * scaleX + imageOffsetX;
    const maxY = Math.max(...vertices.map(v => v.y)) * scaleY + imageOffsetY;
    
    // 하이라이트 박스 생성
    const highlight = document.createElement('div');
    highlight.className = 'text-highlight-active';
    highlight.style.cssText = `
        position: absolute;
        left: ${minX}px;
        top: ${minY}px;
        width: ${Math.max(maxX - minX, 5)}px;
        height: ${Math.max(maxY - minY, 5)}px;
        background-color: rgba(255, 0, 0, 0.2);
        border: 2px solid #ff0000;
        border-radius: 6px;
        pointer-events: none;
        animation: highlightPulse 2s infinite;
        box-shadow: 0 0 20px rgba(255, 0, 0, 0.6);
        z-index: 100;
    `;
    
    overlay.appendChild(highlight);
    overlay.style.display = 'block';
    
    // 디버깅용 로그 (개발 시 필요하면 주석 해제)
    console.log('Highlight:', { minX, minY, maxX, maxY, scaleX, scaleY, imageOffsetX, imageOffsetY });
    console.log('TextBoxes:', textBoxes);
    console.log('Index:', index, 'TextBox:', textBox);
    // console.log('Highlight:', { minX, minY, maxX, maxY, scaleX, scaleY, imageOffsetX, imageOffsetY });
}

/**
 * 모든 하이라이트 제거
 */
function clearHighlight() {
    const overlay = document.getElementById('highlightOverlay');
    overlay.style.display = 'none';
    overlay.innerHTML = '';
    
    // 텍스트 아이템 선택 해제
    document.querySelectorAll('.text-item').forEach(item => {
        item.style.backgroundColor = 'transparent';
        item.style.borderLeft = 'none';
        item.style.color = '';
        
        // 텍스트 색상 원래대로 복원
        const textContent = item.querySelector('.text-content');
        if (textContent) {
            textContent.style.color = 'var(--youtube-text-primary)';
        }
    });
    
    currentHighlightIndex = -1;
}

/* ========================================
   키보드 네비게이션 기능
   ======================================== */

/**
 * 키보드로 텍스트 네비게이션
 * @param {number} direction - 방향 (1: 다음, -1: 이전)
 */
function navigateText(direction) {
    const textItems = document.querySelectorAll('.text-item');
    if (textItems.length === 0) return;
    
    let newIndex = currentHighlightIndex + direction;
    
    // 순환 네비게이션 (처음/끝에서 반대편으로 이동)
    if (newIndex < 0) newIndex = textItems.length - 1;
    if (newIndex >= textItems.length) newIndex = 0;
    
    // 해당 텍스트 아이템 클릭 및 스크롤
    textItems[newIndex].click();
    textItems[newIndex].scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/* ========================================
   이벤트 리스너 등록
   ======================================== */

/**
 * 윈도우 리사이즈 이벤트 처리
 * 화면 크기 변경 시 하이라이트 위치 재조정
 */
let resizeTimeout;
window.addEventListener('resize', function() {
    if (currentHighlightIndex >= 0) {
        // 연속된 리사이즈 이벤트를 방지하기 위해 디바운싱 적용
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(() => {
            drawSingleHighlight(currentHighlightIndex);
        }, 200);
    }
});

/**
 * 이미지 로드 완료 이벤트 처리
 * 이미지 로드 완료 시 하이라이트 재조정
 */
document.addEventListener('DOMContentLoaded', function() {
    const img = document.getElementById('uploadedImage');
    if (img) {
        img.addEventListener('load', function() {
            if (currentHighlightIndex >= 0) {
                setTimeout(() => drawSingleHighlight(currentHighlightIndex), 100);
            }
        });
    }
});

/**
 * 키보드 단축키 처리
 */
document.addEventListener('keydown', function(e) {
    switch(e.key) {
        case 'Escape':
            // Escape 키로 하이라이트 제거
            clearHighlight();
            break;
        case 'ArrowDown':
        case 'ArrowUp':
            // 화살표 키로 텍스트 네비게이션
            e.preventDefault();
            navigateText(e.key === 'ArrowDown' ? 1 : -1);
            break;
    }
});

/* ========================================
   페이지 로드 시 초기화 및 애니메이션
   ======================================== */

/**
 * 페이지 로드 시 실행되는 초기화 함수
 */
document.addEventListener('DOMContentLoaded', function() {
    // Thymeleaf에서 전달된 데이터 재할당
    textBoxes = window.textBoxes || [];
    
    // 하이라이트 기능 디버깅
    console.log('Page loaded. TextBoxes:', textBoxes);
    console.log('Window.textBoxes:', window.textBoxes);
    console.log('Image element:', document.getElementById('uploadedImage'));
    console.log('Overlay element:', document.getElementById('highlightOverlay'));
    // 텍스트 아이템들에 순차적으로 페이드인 애니메이션 적용
    const textItems = document.querySelectorAll('.text-item');
    textItems.forEach((item, index) => {
        setTimeout(() => {
            // 초기 상태 설정 (투명, 아래로 이동)
            item.style.opacity = '0';
            item.style.transform = 'translateY(20px)';
            item.style.transition = 'all 0.5s ease';
            
            // 애니메이션 시작
            setTimeout(() => {
                item.style.opacity = '1';
                item.style.transform = 'translateY(0)';
            }, 50);
        }, index * 100);  // 각 아이템마다 100ms 지연
    });

    // 이미지에 로드 애니메이션 적용
    const img = document.getElementById('uploadedImage');
    if (img) {
        img.style.opacity = '0';
        img.style.transform = 'scale(0.9)';
        img.style.transition = 'all 0.8s ease';
        
        // 200ms 후 애니메이션 시작
        setTimeout(() => {
            img.style.opacity = '1';
            img.style.transform = 'scale(1)';
        }, 200);
    }
});
