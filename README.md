# 🔍 클로바 OCR 테스트 애플리케이션

네이버 클로바 OCR API를 활용한 이미지 텍스트 추출 웹 애플리케이션입니다. 
유튜브 다크 테마 기반의 모던한 UI와 텍스트 하이라이트 기능을 제공합니다.

## 📋 목차

- [주요 기능](#-주요-기능)
- [기술 스택](#️-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [설치 및 실행](#-설치-및-실행)
- [설정 방법](#️-설정-방법)
- [사용 방법](#-사용-방법)
- [API 문서](#-api-문서)
- [개발 가이드](#-개발-가이드)
- [문제 해결](#-문제-해결)

## ✨ 주요 기능

### 🖼️ 이미지 처리
- **드래그 앤 드롭 업로드**: 이미지를 쉽게 업로드할 수 있는 직관적인 인터페이스
- **다양한 이미지 포맷 지원**: JPG, PNG, GIF, BMP 등 주요 이미지 포맷 지원
- **실시간 파일 정보 표시**: 파일명, 크기 등 업로드된 파일 정보 즉시 확인

### 🤖 AI 텍스트 추출
- **네이버 클로바 OCR**: 고정밀 OCR 엔진으로 정확한 텍스트 추출
- **실시간 처리**: 이미지 업로드 즉시 텍스트 추출 시작
- **다국어 지원**: 한국어, 영어 등 다양한 언어 인식

### 🎯 인터랙티브 하이라이트
- **텍스트 위치 표시**: 추출된 텍스트를 클릭하면 이미지에서 해당 위치 하이라이트
- **애니메이션 효과**: 부드러운 펄스 애니메이션으로 시각적 피드백
- **키보드 네비게이션**: 화살표 키로 텍스트 간 이동, Escape 키로 하이라이트 해제

### 📋 텍스트 관리
- **개별 복사**: 각 텍스트를 개별적으로 클립보드에 복사
- **전체 텍스트 보기**: 모든 추출된 텍스트를 하나로 합쳐서 표시
- **스크롤 가능한 목록**: 많은 텍스트도 효율적으로 탐색

### 🎨 모던한 UI/UX
- **유튜브 다크 테마**: 눈에 편안한 다크 모드 디자인
- **글래스모피즘**: 현대적인 반투명 효과
- **반응형 디자인**: 데스크톱, 태블릿, 모바일 모든 기기 지원
- **부드러운 애니메이션**: 페이드인, 호버 효과 등 세련된 인터랙션

## 🛠️ 기술 스택

### Backend
- **Spring Boot 3.5.5**: 메인 프레임워크
- **Spring Web**: 웹 MVC 및 REST API
- **Thymeleaf**: 서버사이드 템플릿 엔진
- **Jackson**: JSON 파싱 및 처리
- **Java 17**: 최신 LTS 버전

### Frontend  
- **HTML5**: 시맨틱 마크업
- **CSS3**: 모던한 스타일링 (Flexbox, Grid, 애니메이션)
- **JavaScript ES6+**: 인터랙티브 기능
- **Bootstrap 5.1.3**: 반응형 그리드 시스템
- **Font Awesome 6.0**: 아이콘 라이브러리

### External API
- **네이버 클로바 OCR**: 고정밀 텍스트 인식 API

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/example/ocr_test/
│   │   ├── OcrTestApplication.java          # 메인 애플리케이션 클래스
│   │   ├── controller/
│   │   │   └── OcrController.java           # 웹 요청 처리 컨트롤러
│   │   └── service/
│   │       └── ClovaOcrService.java         # OCR API 연동 서비스
│   └── resources/
│       ├── application.properties           # 애플리케이션 설정
│       ├── static/                         # 정적 리소스
│       │   ├── css/
│       │   │   └── result.css              # 결과 페이지 스타일
│       │   └── js/
│       │       └── result.js               # 결과 페이지 스크립트
│       └── templates/                      # Thymeleaf 템플릿
│           ├── index.html                  # 메인 페이지
│           └── result.html                 # 결과 페이지
└── test/
    └── java/com/example/ocr_test/
        └── OcrTestApplicationTests.java     # 테스트 클래스
```

## 🚀 설치 및 실행

### 사전 요구사항
- **Java 17** 이상
- **네이버 클라우드 플랫폼** 계정 및 클로바 OCR 서비스 신청

### 실행 방법

1. **프로젝트 클론**
   ```bash
   git clone https://github.com/jitae1214/CLOVA-OCR-Test.git
   cd ocr_test
   ```

2. **설정 파일 수정**
   ```bash
   # src/main/resources/application.properties 편집
   ```

3. **애플리케이션 실행**
   ```bash
   # Windows
   ./gradlew.bat bootRun
   
   # macOS/Linux  
   ./gradlew bootRun
   ```

4. **브라우저에서 접속**
   ```
   http://localhost:8080
   ```

## ⚙️ 설정 방법

### 네이버 클로바 OCR API 설정

1. **네이버 클라우드 플랫폼 가입**
   - [네이버 클라우드 플랫폼](https://www.ncloud.com/) 접속
   - 계정 생성 및 로그인

2. **클로바 OCR 서비스 신청**
   - AI Service > CLOVA OCR 메뉴 이동
   - 서비스 신청 및 승인 대기

3. **API 키 발급**
   - API Gateway에서 새 게이트웨이 생성
   - OCR API 연동 설정
   - Secret Key 발급

4. **설정 파일 작성**
   ```bash
   # 예제 설정 파일을 실제 설정 파일로 복사
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
   
   ```properties
   # src/main/resources/application.properties 편집
   
   # 클로바 OCR API 설정 (실제 발급받은 값으로 변경)
   clova.ocr.api-url=https://your-actual-api-gateway-url
   clova.ocr.secret-key=your-actual-secret-key
   
   # 기타 설정은 그대로 유지
   ```
   
   ⚠️ **보안 주의사항**: `application.properties` 파일은 `.gitignore`에 포함되어 있어 Git에 커밋되지 않습니다.

## 📖 사용 방법

### 1. 이미지 업로드
- 메인 페이지에서 이미지 파일을 클릭하거나 드래그 앤 드롭으로 업로드
- 지원 형식: JPG, PNG, GIF, BMP

### 2. 텍스트 추출 결과 확인
- 업로드 후 자동으로 OCR 처리 시작
- 추출된 텍스트 목록이 번호와 함께 표시

### 3. 하이라이트 기능 사용
- 텍스트 목록에서 원하는 텍스트 클릭
- 이미지에서 해당 텍스트 위치가 빨간색 박스로 하이라이트
- "하이라이트 지우기" 버튼으로 선택 해제

### 4. 텍스트 복사
- 각 텍스트 항목 옆의 복사 버튼으로 개별 복사
- "전체 텍스트 보기"에서 모든 텍스트를 한 번에 확인

### 5. 키보드 단축키
- `↑/↓ 화살표`: 텍스트 네비게이션
- `Escape`: 하이라이트 해제

## 📚 API 문서

### 주요 엔드포인트

| Method | URL | 설명 | 파라미터 |
|--------|-----|------|----------|
| GET | `/` | 메인 페이지 | - |
| POST | `/upload` | 이미지 업로드 및 OCR 처리 | `imageFile`: MultipartFile |
| GET | `/image/{imageId}` | 업로드된 이미지 조회 | `imageId`: String |

### 응답 데이터 구조

```javascript
// OCR 결과 모델 데이터
{
    fileName: "example.jpg",           // 원본 파일명
    fileSize: "1.2 MB",               // 포맷된 파일 크기
    extractedTexts: ["텍스트1", "텍스트2"], // 추출된 텍스트 배열
    textBoxes: [                      // 텍스트 좌표 정보
        {
            text: "텍스트1",
            vertices: [               // 4개 꼭짓점 좌표
                {x: 50, y: 100},     // 좌상단
                {x: 200, y: 100},    // 우상단  
                {x: 200, y: 130},    // 우하단
                {x: 50, y: 130}      // 좌하단
            ]
        }
    ],
    imageId: "1234567890",            // 이미지 식별자
    message: "총 2개의 텍스트를 추출했습니다."
}
```

## 🔧 개발 가이드

### 코드 구조 설명

#### 1. 컨트롤러 (OcrController.java)
```java
// 메인 페이지 표시
@GetMapping("/")

// 이미지 업로드 및 OCR 처리  
@PostMapping("/upload")

// 업로드된 이미지 서빙
@GetMapping("/image/{imageId}")
```

#### 2. 서비스 (ClovaOcrService.java)
```java
// OCR API 호출 및 결과 파싱
public Map<String, Object> extractTextFromImage(MultipartFile imageFile)

// OCR 응답에서 텍스트와 좌표 추출
private Map<String, Object> parseOcrResponseWithCoordinates(String responseJson)
```

#### 3. 프론트엔드 구조
```
templates/
├── index.html          # 업로드 페이지
└── result.html         # 결과 표시 페이지

static/
├── css/result.css      # 스타일시트 (유튜브 다크 테마)
└── js/result.js        # 하이라이트 및 복사 기능
```

### 주요 기능별 구현

#### 1. 이미지 하이라이트
```javascript
// 텍스트 클릭 시 호출
function highlightTextBox(element)

// 실제 하이라이트 박스 그리기
function drawSingleHighlight(index)

// 하이라이트 제거
function clearHighlight()
```

#### 2. 좌표 변환 로직
```javascript
// OCR 원본 좌표 → 화면 표시 좌표 변환
const scaleX = actualImageWidth / img.naturalWidth;
const scaleY = actualImageHeight / img.naturalHeight;
```

#### 3. 반응형 레이아웃
```css
/* 데스크톱: 이미지 7:5 텍스트 */
.col-xl-7, .col-xl-5

/* 태블릿: 이미지 8:4 텍스트 */  
.col-lg-8, .col-lg-4

/* 모바일: 세로 배치 */
.col-md-12
```

## 🛠️ 문제 해결

### 자주 발생하는 문제

#### 1. OCR API 설정 오류
```
증상: "클로바 OCR API URL과 Secret Key가 설정되지 않았습니다"
해결: application.properties에 올바른 API URL과 Secret Key 설정
```

#### 2. 하이라이트가 표시되지 않음
```
증상: 텍스트 클릭 시 이미지에 하이라이트가 나타나지 않음
원인: textBoxes 데이터 부족 또는 JavaScript 로딩 실패
해결: 브라우저 개발자 도구에서 콘솔 로그 확인
```

#### 3. 이미지가 표시되지 않음
```
증상: 결과 페이지에서 이미지가 로드되지 않음
원인: 이미지 저장소 문제 또는 잘못된 이미지 ID
해결: 서버 로그에서 이미지 저장 과정 확인
```

#### 4. CSS/JS 파일 로딩 실패
```
증상: 스타일이나 JavaScript 기능이 작동하지 않음
원인: static 폴더 경로 문제
해결: /css/result.css, /js/result.js 경로 확인
```

### 로그 레벨 설정
```properties
# application.properties에 추가
logging.level.com.example.ocr_test=DEBUG
logging.level.root=INFO
```

## 🔄 개발 팁

### 1. 로컬 개발 시 더미 데이터 사용
현재 코드에는 OCR API 연동이 없을 때를 위한 더미 좌표 데이터가 포함되어 있습니다:

```java
// OcrController.java에서 더미 데이터 생성
if (textBoxes.isEmpty() && !extractedTexts.isEmpty()) {
    // 테스트용 더미 좌표 생성
}
```

### 2. 프로덕션 배포 시 주의사항
- 더미 데이터 생성 코드 제거
- 이미지 저장소를 메모리에서 영구 저장소로 변경
- API 키 환경변수로 관리
- HTTPS 적용

### 3. 성능 최적화
- 이미지 크기 제한 설정
- 캐싱 전략 적용
- CDN 사용 고려

## 📄 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.

## 👥 기여하기

### Git 커밋 전 체크리스트
- [ ] `application.properties`에 실제 API 키가 포함되어 있지 않은지 확인
- [ ] `.gitignore`에 보안 파일들이 제대로 설정되어 있는지 확인
- [ ] 테스트 코드 작성 및 통과 확인

### 기여 절차
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 보안 가이드
```bash
# 커밋 전 확인사항
git status                    # 커밋될 파일 목록 확인
git diff src/main/resources/  # 설정 파일 변경사항 확인

# application.properties가 목록에 있으면 안됨
# 있다면 .gitignore 설정 확인 필요
```

## 📞 문의사항

프로젝트에 대한 문의사항이나 버그 리포트는 GitHub Issues를 통해 등록해주세요.

---
