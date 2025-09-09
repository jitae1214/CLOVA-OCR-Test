/**
 * 네이버 클로바 OCR API 연동 서비스
 * 
 * 이미지 파일을 받아서 클로바 OCR API에 전송하고,
 * 응답을 파싱하여 텍스트와 좌표 정보를 추출하는 서비스입니다.
 */
package com.example.ocr_test.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 클로바 OCR 서비스 클래스
 * 
 * 네이버 클로바 OCR API와의 통신을 담당하며,
 * 이미지에서 텍스트를 추출하고 좌표 정보를 함께 반환합니다.
 */
@Service
public class ClovaOcrService {

    /**
     * 클로바 OCR API 엔드포인트 URL
     * application.properties에서 설정값을 주입받습니다.
     */
    @Value("${clova.ocr.api-url:}")
    private String apiUrl;

    /**
     * 클로바 OCR API 인증을 위한 Secret Key
     * application.properties에서 설정값을 주입받습니다.
     */
    @Value("${clova.ocr.secret-key:}")
    private String secretKey;

    public Map<String, Object> extractTextFromImage(MultipartFile imageFile) throws IOException {
        if (apiUrl.isEmpty() || secretKey.isEmpty()) {
            throw new IllegalStateException("클로바 OCR API URL과 Secret Key가 설정되지 않았습니다. application.properties를 확인해주세요.");
        }

        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";
        
        // 메시지 JSON 생성
        String message = String.format("""
            {
                "version": "V2",
                "requestId": "%s",
                "timestamp": %d,
                "images": [
                    {
                        "format": "%s",
                        "name": "%s"
                    }
                ]
            }""", 
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            getFileExtension(imageFile.getOriginalFilename()),
            imageFile.getOriginalFilename()
        );

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("X-OCR-SECRET", secretKey);

        try (OutputStream outputStream = connection.getOutputStream()) {
            
            // message 파트 추가
            outputStream.write(("--" + boundary + LINE_FEED).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Disposition: form-data; name=\"message\"" + LINE_FEED).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Type: text/plain; charset=UTF-8" + LINE_FEED).getBytes(StandardCharsets.UTF_8));
            outputStream.write(LINE_FEED.getBytes(StandardCharsets.UTF_8));
            outputStream.write(message.getBytes(StandardCharsets.UTF_8));
            outputStream.write(LINE_FEED.getBytes(StandardCharsets.UTF_8));

            // file 파트 추가
            outputStream.write(("--" + boundary + LINE_FEED).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + 
                               imageFile.getOriginalFilename() + "\"" + LINE_FEED).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("Content-Type: " + (imageFile.getContentType() != null ? 
                               imageFile.getContentType() : "image/jpeg") + LINE_FEED).getBytes(StandardCharsets.UTF_8));
            outputStream.write(LINE_FEED.getBytes(StandardCharsets.UTF_8));
            
            // 파일 바이너리 데이터 쓰기
            outputStream.write(imageFile.getBytes());
            
            // 마지막 바운더리
            outputStream.write(LINE_FEED.getBytes(StandardCharsets.UTF_8));
            outputStream.write(("--" + boundary + "--" + LINE_FEED).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        // 응답 읽기
        int responseCode = connection.getResponseCode();
        InputStream inputStream = (responseCode >= 200 && responseCode < 300) ? 
                                 connection.getInputStream() : connection.getErrorStream();
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        if (responseCode != 200) {
            throw new RuntimeException("OCR API 호출 실패: " + responseCode + " - " + response.toString());
        }

        return parseOcrResponseWithCoordinates(response.toString());
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private Map<String, Object> parseOcrResponseWithCoordinates(String responseJson) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        
        List<String> extractedTexts = new ArrayList<>();
        List<Map<String, Object>> textBoxes = new ArrayList<>();
        
        JsonNode imagesNode = rootNode.path("images");
        if (imagesNode.isArray()) {
            for (JsonNode imageNode : imagesNode) {
                JsonNode fieldsNode = imageNode.path("fields");
                if (fieldsNode.isArray()) {
                    for (JsonNode fieldNode : fieldsNode) {
                        String inferText = fieldNode.path("inferText").asText();
                        if (!inferText.isEmpty()) {
                            extractedTexts.add(inferText);
                            
                            // 좌표 정보 추출
                            JsonNode boundingPolyNode = fieldNode.path("boundingPoly");
                            if (!boundingPolyNode.isMissingNode()) {
                                JsonNode verticesNode = boundingPolyNode.path("vertices");
                                if (verticesNode.isArray() && verticesNode.size() >= 4) {
                                    Map<String, Object> textBox = new HashMap<>();
                                    textBox.put("text", inferText);
                                    
                                    List<Map<String, Integer>> vertices = new ArrayList<>();
                                    for (JsonNode vertex : verticesNode) {
                                        Map<String, Integer> point = new HashMap<>();
                                        point.put("x", vertex.path("x").asInt());
                                        point.put("y", vertex.path("y").asInt());
                                        vertices.add(point);
                                    }
                                    textBox.put("vertices", vertices);
                                    textBoxes.add(textBox);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("extractedTexts", extractedTexts);
        result.put("textBoxes", textBoxes);
        
        return result;
    }
}
