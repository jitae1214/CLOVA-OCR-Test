package com.example.ocr_test.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 구글 지도 API 연동 서비스
 * 
 * 위치 좌표를 받아서 도로명 주소로 변환하는 서비스입니다.
 */
@Service
public class GoogleMapsService {

    @Value("${google.places.api.key:}")
    private String apiKey;

    private static final String REVERSE_GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    /**
     * 위도, 경도를 받아서 도로명 주소를 반환합니다.
     * 
     * @param latitude 위도
     * @param longitude 경도
     * @return 도로명 주소
     */
    public String getAddressFromCoordinates(double latitude, double longitude) throws IOException {
        if (apiKey.isEmpty()) {
            throw new IllegalStateException("Google Places API 키가 설정되지 않았습니다. application.properties를 확인해주세요.");
        }

        // 구글 지오코딩 API 요청 URL 생성
        String requestUrl = String.format("%s?latlng=%.6f,%.6f&key=%s&language=ko",
                REVERSE_GEOCODING_URL, latitude, longitude, apiKey);

        System.out.println("DEBUG: Google Maps API 요청 URL: " + requestUrl);

        // API 호출
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        System.out.println("DEBUG: Google Maps API 응답 코드: " + responseCode);

        if (responseCode != 200) {
            throw new RuntimeException("Google Maps API 호출 실패: " + responseCode);
        }

        // 응답 읽기
        StringBuilder response = new StringBuilder();
        try (var reader = connection.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
        }

        // 응답 파싱
        return parseAddressFromResponse(response.toString());
    }

    /**
     * 구글 지오코딩 API 응답에서 도로명 주소를 추출합니다.
     */
    private String parseAddressFromResponse(String responseJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseJson);

        System.out.println("DEBUG: Google Maps API 응답: " + responseJson);

        String status = rootNode.path("status").asText();
        if (!"OK".equals(status)) {
            throw new RuntimeException("Google Maps API 오류: " + status);
        }

        JsonNode resultsNode = rootNode.path("results");
        if (resultsNode.isArray() && resultsNode.size() > 0) {
            JsonNode firstResult = resultsNode.get(0);
            String formattedAddress = firstResult.path("formatted_address").asText();
            
            System.out.println("DEBUG: 추출된 주소: " + formattedAddress);
            return formattedAddress;
        }

        throw new RuntimeException("주소를 찾을 수 없습니다.");
    }

    /**
     * 현재 위치를 가져오는 기능 (브라우저의 Geolocation API 사용)
     * 서버에서는 기본 위치를 제공합니다.
     */
    public double[] getDefaultLocation() {
        // 서울시청 좌표를 기본값으로 사용
        return new double[]{37.5665, 126.9780}; // [위도, 경도]
    }
}
