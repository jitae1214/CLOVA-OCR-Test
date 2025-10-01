package com.example.ocr_test.controller;

import com.example.ocr_test.service.GoogleMapsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 지도 위치 선택 컨트롤러
 * 
 * 구글 지도를 사용하여 위치를 선택하고 도로명 주소를 가져오는 컨트롤러입니다.
 */
@Controller
public class MapController {

    @Autowired
    private GoogleMapsService googleMapsService;

    @Value("${google.places.api.key:}")
    private String apiKey;

    /**
     * 지도 페이지를 표시합니다.
     */
    @GetMapping("/map")
    public String mapPage(Model model) {
        // 구글 지도 API 키가 설정되어 있는지 확인
        if (apiKey.isEmpty()) {
            model.addAttribute("error", "Google Maps API 키가 설정되지 않았습니다. application.properties를 확인해주세요.");
            model.addAttribute("configHelp", true);
        } else {
            model.addAttribute("apiKey", apiKey);
        }

        // 기본 위치 설정 (서울시청)
        double[] defaultLocation = googleMapsService.getDefaultLocation();
        model.addAttribute("defaultLatitude", defaultLocation[0]);
        model.addAttribute("defaultLongitude", defaultLocation[1]);

        return "map";
    }

    /**
     * 선택된 위치의 주소를 가져옵니다.
     */
    @PostMapping("/get-address")
    public String getAddress(@RequestParam("latitude") double latitude,
                           @RequestParam("longitude") double longitude,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            // 구글 지오코딩 API로 주소 조회
            String address = googleMapsService.getAddressFromCoordinates(latitude, longitude);
            
            // 결과 모델에 추가
            model.addAttribute("latitude", latitude);
            model.addAttribute("longitude", longitude);
            model.addAttribute("address", address);
            
            return "map-result";
            
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("configHelp", true);
            return "redirect:/map";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "주소 조회 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/map";
        }
    }

    /**
     * 지도 결과 페이지를 표시합니다.
     */
    @GetMapping("/map-result")
    public String mapResultPage() {
        return "map-result";
    }
}
