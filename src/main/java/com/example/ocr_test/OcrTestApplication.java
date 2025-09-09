package com.example.ocr_test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OCR 테스트 애플리케이션 메인 클래스
 * 
 * 이 애플리케이션은 네이버 클로바 OCR API를 사용하여 
 * 이미지에서 텍스트를 추출하는 웹 애플리케이션입니다.
 * 
 * 주요 기능:
 * - 이미지 업로드 및 OCR 처리
 * - 추출된 텍스트 목록 표시
 * - 텍스트 클릭 시 이미지에서 해당 위치 하이라이트
 * - 개별/전체 텍스트 복사 기능
 * - 유튜브 다크 테마 기반의 모던한 UI
 */
@SpringBootApplication
public class OcrTestApplication {

    /**
     * 애플리케이션 시작점
     * Spring Boot 애플리케이션을 초기화하고 실행합니다.
     * 
     * @param args 커맨드라인 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(OcrTestApplication.class, args);
    }
}
