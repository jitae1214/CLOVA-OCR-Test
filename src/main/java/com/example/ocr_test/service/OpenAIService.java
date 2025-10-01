package com.example.ocr_test.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI API 연동 서비스
 * 
 * OCR로 추출된 텍스트를 OpenAI API를 통해 분석하여
 * 메뉴 이름만 추출하는 서비스입니다.
 */
@Service
public class OpenAIService {

    @Value("${openai.api.key:}")
    private String apiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    /**
     * OCR로 추출된 텍스트를 분석하여 메뉴 이름만 추출합니다.
     * 
     * @param extractedTexts OCR로 추출된 텍스트 목록
     * @return 분석된 메뉴 이름 목록
     */
    public List<String> analyzeMenuItems(List<String> extractedTexts) throws IOException {
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API 키가 설정되지 않았습니다. application.properties를 확인해주세요.");
        }

        // 모든 텍스트를 하나로 합치기
        String combinedText = String.join("\n", extractedTexts);

        // OpenAI API 요청 페이로드 생성
        String requestBody = createRequestBody(combinedText);

        // API 호출
        String response = callOpenAIAPI(requestBody);

        // 응답 파싱하여 메뉴 이름 추출
        return parseMenuItems(response);
    }

    private String createRequestBody(String text) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            
            // 텍스트 정리 및 이스케이프 처리
            String cleanedText = cleanTextForJson(text);
            
            // JSON 구조 생성
            String systemMessage = "당신은 메뉴판 분석 전문가입니다. 주어진 텍스트에서 음식 메뉴 이름만 추출해주세요. 가격, 설명, 기타 정보는 제외하고 순수한 메뉴 이름만 추출하세요. 각 메뉴는 한 줄씩 구분하여 JSON 배열 형태로 응답해주세요.";
            String userMessage = "다음 텍스트에서 음식 메뉴 이름만 추출해주세요:\n\n" + cleanedText;
            
            // JSON 객체 구성
            var requestData = new java.util.HashMap<String, Object>();
            requestData.put("model", "gpt-3.5-turbo");
            
            var messages = new java.util.ArrayList<Object>();
            
            var systemMsg = new java.util.HashMap<String, String>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemMessage);
            messages.add(systemMsg);
            
            var userMsg = new java.util.HashMap<String, String>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            requestData.put("messages", messages);
            requestData.put("max_tokens", 1000);
            requestData.put("temperature", 0.1);
            
            return objectMapper.writeValueAsString(requestData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 생성 중 오류 발생: " + e.getMessage());
        }
    }
    
    /**
     * JSON에 안전하게 포함될 수 있도록 텍스트를 정리합니다.
     */
    private String cleanTextForJson(String text) {
        if (text == null) return "";
        
        return text
            .replace("\\", "\\\\")  // 백슬래시 이스케이프
            .replace("\"", "\\\"")  // 따옴표 이스케이프
            .replace("\n", "\\n")   // 줄바꿈을 \\n으로 변경
            .replace("\r", "\\r")   // 캐리지 리턴을 \\r으로 변경
            .replace("\t", "\\t");  // 탭을 \\t으로 변경
    }

    private String callOpenAIAPI(String requestBody) throws IOException {
        System.out.println("DEBUG: OpenAI API 요청 시작");
        System.out.println("DEBUG: API Key 길이: " + (apiKey != null ? apiKey.length() : "null"));
        System.out.println("DEBUG: 요청 Body: " + requestBody);
        
        URL url = new URL(OPENAI_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(requestBody);
            writer.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("DEBUG: 응답 코드: " + responseCode);
        
        // 에러 응답 읽기
        String errorResponse = "";
        if (responseCode != 200) {
            try (var errorStream = connection.getErrorStream()) {
                if (errorStream != null) {
                    errorResponse = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("DEBUG: 에러 응답: " + errorResponse);
                }
            }
            throw new RuntimeException("OpenAI API 호출 실패: " + responseCode + " - " + errorResponse);
        }

        StringBuilder response = new StringBuilder();
        try (var reader = connection.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
        }

        return response.toString();
    }

    private List<String> parseMenuItems(String responseJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        
        List<String> menuItems = new ArrayList<>();
        
        JsonNode choicesNode = rootNode.path("choices");
        if (choicesNode.isArray() && choicesNode.size() > 0) {
            JsonNode firstChoice = choicesNode.get(0);
            JsonNode messageNode = firstChoice.path("message");
            String content = messageNode.path("content").asText();
            
            System.out.println("DEBUG: OpenAI 응답 내용: " + content);
            
            // JSON 배열 형태로 응답이 오는 경우 파싱
            if (content.trim().startsWith("[")) {
                try {
                    JsonNode menuArray = objectMapper.readTree(content);
                    if (menuArray.isArray()) {
                        for (JsonNode menuItem : menuArray) {
                            String item = menuItem.asText().trim();
                            if (!item.isEmpty()) {
                                menuItems.add(item);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG: JSON 파싱 실패, 텍스트 파싱으로 전환: " + e.getMessage());
                    // JSON 파싱 실패 시 줄 단위로 분할
                    parseTextContent(content, menuItems);
                }
            } else {
                // 일반 텍스트 형태로 응답이 오는 경우 줄 단위로 분할
                parseTextContent(content, menuItems);
            }
        }
        
        System.out.println("DEBUG: 최종 메뉴 목록: " + menuItems);
        return menuItems;
    }
    
    /**
     * 텍스트 내용을 파싱하여 메뉴 목록을 추출합니다.
     */
    private void parseTextContent(String content, List<String> menuItems) {
        // 마크다운 코드 블록 제거
        content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        
        String[] lines = content.split("\n");
        for (String line : lines) {
            String cleanedLine = line.trim()
                .replaceAll("^[\\[\\]\"\\-\\s]+", "") // 시작 부분의 특수문자 제거
                .replaceAll("[\\[\\]\"\\s,]+$", "") // 끝 부분의 특수문자 제거
                .replaceAll("^\"|\"$", "") // 양 끝의 따옴표 제거
                .replaceAll("\\s*\\d+원.*$", "") // 가격 정보 제거
                .replaceAll("^\\d+\\.\\s*", ""); // 번호 제거 (1. 2. 등)
            
            if (!cleanedLine.isEmpty() && 
                !cleanedLine.equals(",") && 
                !cleanedLine.matches("^\\d+$") &&
                !cleanedLine.equals("[") &&
                !cleanedLine.equals("]") &&
                !cleanedLine.startsWith("```") && // 마크다운 코드 블록 시작 제거
                !cleanedLine.equals("```")) { // 마크다운 코드 블록 끝 제거
                menuItems.add(cleanedLine);
            }
        }
    }
}
