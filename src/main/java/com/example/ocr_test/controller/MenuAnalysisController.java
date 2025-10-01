package com.example.ocr_test.controller;

import com.example.ocr_test.service.ClovaOcrService;
import com.example.ocr_test.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 메뉴 분석 컨트롤러
 * 
 * 메뉴판 이미지를 업로드하여 OCR로 텍스트를 추출하고,
 * OpenAI API를 통해 메뉴 이름만 분석하여 제공하는 컨트롤러입니다.
 */
@Controller
public class MenuAnalysisController {

    @Autowired
    private ClovaOcrService clovaOcrService;

    @Autowired
    private OpenAIService openAIService;

    /**
     * 메뉴 분석 페이지를 표시합니다.
     */
    @GetMapping("/menu-analysis")
    public String menuAnalysisPage() {
        return "menu-analysis";
    }

    /**
     * 메뉴판 이미지를 업로드하고 분석합니다.
     */
    @PostMapping("/analyze-menu")
    public String analyzeMenu(@RequestParam("imageFile") MultipartFile imageFile,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        
        // 파일 유효성 검사
        if (imageFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "이미지 파일을 선택해주세요.");
            return "redirect:/menu-analysis";
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            redirectAttributes.addFlashAttribute("error", "이미지 파일만 업로드 가능합니다.");
            return "redirect:/menu-analysis";
        }

        try {
            // 1단계: OCR로 텍스트 추출
            Map<String, Object> ocrResult = clovaOcrService.extractTextFromImage(imageFile);
            @SuppressWarnings("unchecked")
            List<String> extractedTexts = (List<String>) ocrResult.get("extractedTexts");
            
            if (extractedTexts.isEmpty()) {
                model.addAttribute("error", "이미지에서 텍스트를 찾을 수 없습니다. 더 선명한 이미지를 업로드해주세요.");
                return "menu-analysis";
            }

            // 2단계: OpenAI로 메뉴 분석
            List<String> menuItems = openAIService.analyzeMenuItems(extractedTexts);
            
            // 결과 모델에 추가
            model.addAttribute("fileName", imageFile.getOriginalFilename());
            model.addAttribute("fileSize", formatFileSize(imageFile.getSize()));
            model.addAttribute("extractedTexts", extractedTexts);
            model.addAttribute("menuItems", menuItems);
            model.addAttribute("originalTextCount", extractedTexts.size());
            model.addAttribute("menuItemCount", menuItems.size());
            
            if (menuItems.isEmpty()) {
                model.addAttribute("message", "메뉴를 찾을 수 없습니다. 다른 이미지를 시도해보세요.");
            } else {
                model.addAttribute("message", 
                    String.format("총 %d개의 메뉴를 찾았습니다.", menuItems.size()));
            }
            
            return "menu-result";
            
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("configHelp", true);
            return "redirect:/menu-analysis";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/menu-analysis";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "메뉴 분석 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/menu-analysis";
        }
    }

    /**
     * 파일 크기를 읽기 쉬운 형태로 포맷합니다.
     */
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
