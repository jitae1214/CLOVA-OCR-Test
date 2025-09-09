package com.example.ocr_test.controller;

import com.example.ocr_test.service.ClovaOcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class OcrController {

    @Autowired
    private ClovaOcrService clovaOcrService;
    
    // 임시 이미지 저장소 (실제 운영에서는 파일 시스템이나 클라우드 스토리지 사용)
    private final Map<String, byte[]> imageStorage = new ConcurrentHashMap<>();

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String uploadImage(@RequestParam("imageFile") MultipartFile imageFile, 
                            Model model, 
                            RedirectAttributes redirectAttributes) {
        
        // 파일이 비어있는지 확인
        if (imageFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "이미지 파일을 선택해주세요.");
            return "redirect:/";
        }

        // 이미지 파일 형식 확인
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            redirectAttributes.addFlashAttribute("error", "이미지 파일만 업로드 가능합니다.");
            return "redirect:/";
        }

        try {
            // OCR 서비스 호출
            Map<String, Object> ocrResult = clovaOcrService.extractTextFromImage(imageFile);
            @SuppressWarnings("unchecked")
            List<String> extractedTexts = (List<String>) ocrResult.get("extractedTexts");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> textBoxes = (List<Map<String, Object>>) ocrResult.get("textBoxes");
            
            // 임시 테스트: textBoxes가 비어있으면 더미 데이터 추가
            System.out.println("DEBUG: textBoxes.isEmpty() = " + textBoxes.isEmpty());
            System.out.println("DEBUG: extractedTexts.isEmpty() = " + extractedTexts.isEmpty());
            System.out.println("DEBUG: extractedTexts.size() = " + extractedTexts.size());
            
            if (textBoxes.isEmpty() && !extractedTexts.isEmpty()) {
                System.out.println("DEBUG: 더미 textBoxes 데이터 생성 중...");
                for (int i = 0; i < extractedTexts.size(); i++) {
                    Map<String, Object> dummyTextBox = new HashMap<>();
                    dummyTextBox.put("text", extractedTexts.get(i));
                    
                    List<Map<String, Integer>> vertices = new ArrayList<>();
                    // 더미 좌표 (세로로 배치)
                    int startY = 50 + (i * 40);
                    vertices.add(Map.of("x", 50, "y", startY));
                    vertices.add(Map.of("x", 400, "y", startY));
                    vertices.add(Map.of("x", 400, "y", startY + 30));
                    vertices.add(Map.of("x", 50, "y", startY + 30));
                    
                    dummyTextBox.put("vertices", vertices);
                    textBoxes.add(dummyTextBox);
                }
                System.out.println("DEBUG: 더미 데이터 생성 완료. textBoxes.size() = " + textBoxes.size());
            }
            
            // 이미지를 임시 저장소에 저장
            String imageId = String.valueOf(System.currentTimeMillis());
            imageStorage.put(imageId, imageFile.getBytes());
            
            model.addAttribute("fileName", imageFile.getOriginalFilename());
            model.addAttribute("fileSize", formatFileSize(imageFile.getSize()));
            model.addAttribute("extractedTexts", extractedTexts);
            model.addAttribute("textBoxes", textBoxes);
            model.addAttribute("imageId", imageId);
            
            System.out.println("DEBUG: 모델에 추가된 textBoxes 크기: " + textBoxes.size());
            
            if (extractedTexts.isEmpty()) {
                model.addAttribute("message", "이미지에서 텍스트를 찾을 수 없습니다.");
            } else {
                model.addAttribute("message", "총 " + extractedTexts.size() + "개의 텍스트를 추출했습니다.");
            }
            
            return "result";
            
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("configHelp", true);
            return "index";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "OCR 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<ByteArrayResource> getImage(@PathVariable String imageId) {
        byte[] imageData = imageStorage.get(imageId);
        if (imageData == null) {
            return ResponseEntity.notFound().build();
        }
        
        ByteArrayResource resource = new ByteArrayResource(imageData);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.IMAGE_JPEG) // 기본값, 실제로는 원본 타입을 저장해야 함
                .contentLength(imageData.length)
                .body(resource);
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " bytes";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}
