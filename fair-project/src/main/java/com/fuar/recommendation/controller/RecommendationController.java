package com.fuar.recommendation.controller;

import com.fuar.dto.UserDTO;
import com.fuar.model.User;
import com.fuar.recommendation.dto.RecommendationDTO;
import com.fuar.recommendation.dto.RecommendationDetailDTO;
import com.fuar.recommendation.dto.RecommendationReasonDTO;
import com.fuar.recommendation.model.RecommendationReason;
import com.fuar.recommendation.model.UserRecommendation;
import com.fuar.recommendation.service.RecommendationService;
import com.fuar.service.UserService;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bu controller, öneri sistemiyle ilgili REST API endpoint'lerini sağlar.
 */
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recommendations", description = "Kullanıcı öneri sistemi API'leri")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserService userService;

    /**
     * Oturum açmış kullanıcı için önerileri getirir.
     * 
     * @param userDetails Oturum açmış kullanıcı detayları
     * @param limit Sonuç sayısı sınırı (varsayılan: 10)
     * @return Öneri listesi
     */
    @GetMapping
    @Operation(summary = "Kullanıcı için önerileri getirir", 
               description = "Oturum açmış kullanıcı için benzer kullanıcı önerilerini getirir")
    public ResponseEntity<List<RecommendationDTO>> getRecommendationsForCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit) {
        
        User user = userService.findUserByUsername(userDetails.getUsername());
        
        List<UserRecommendation> recommendations = recommendationService.getRecommendationsForUser(user.getId(), limit);
        
        List<RecommendationDTO> dtos = recommendations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Belirli bir kullanıcı için önerileri getirir (yöneticiler için).
     * 
     * @param userId Kullanıcı ID'si
     * @param limit Sonuç sayısı sınırı (varsayılan: 10)
     * @return Öneri listesi
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Belirli bir kullanıcı için önerileri getirir", 
               description = "Belirli bir kullanıcı için benzer kullanıcı önerilerini getirir (yöneticiler için)")
    public ResponseEntity<List<RecommendationDTO>> getRecommendationsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<UserRecommendation> recommendations = recommendationService.getRecommendationsForUser(userId, limit);
        
        List<RecommendationDTO> dtos = recommendations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Oturum açmış kullanıcı için detaylı önerileri (nedenlerle birlikte) getirir.
     * 
     * @param userDetails Oturum açmış kullanıcı detayları
     * @param limit Sonuç sayısı sınırı (varsayılan: 10)
     * @return Detaylı öneri listesi
     */
    @GetMapping("/detailed")
    @Operation(summary = "Kullanıcı için detaylı önerileri getirir", 
               description = "Oturum açmış kullanıcı için benzer kullanıcı önerilerini ve nedenlerini getirir")
    public ResponseEntity<List<RecommendationDetailDTO>> getDetailedRecommendationsForCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit) {
        
        User user = userService.findUserByUsername(userDetails.getUsername());
        
        Map<UserRecommendation, List<RecommendationReason>> recommendationsWithReasons = 
                recommendationService.getRecommendationsWithReasonsForUser(user.getId(), limit);
        
        List<RecommendationDetailDTO> dtos = recommendationsWithReasons.entrySet().stream()
                .map(entry -> convertToDetailDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Belirli bir öneri için detayları getirir.
     * 
     * @param recommendationId Öneri ID'si
     * @return Detaylı öneri bilgisi
     */
    @GetMapping("/{recommendationId}")
    @Operation(summary = "Belirli bir öneri için detayları getirir", 
               description = "Belirli bir öneri için detaylı bilgi ve nedenler getirir")
    public ResponseEntity<RecommendationDetailDTO> getRecommendationDetails(
            @PathVariable Long recommendationId) {
        
        UserRecommendation recommendation = recommendationService.getRecommendationsForUser(null, 100).stream()
                .filter(r -> r.getId().equals(recommendationId))
                .findFirst()
                .orElse(null);
        
        if (recommendation == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<RecommendationReason> reasons = recommendationService.getReasonsForRecommendation(recommendationId);
        
        RecommendationDetailDTO dto = convertToDetailDTO(recommendation, reasons);
        
        return ResponseEntity.ok(dto);
    }
    
    /**
     * Bir öneri durumunu günceller (örn. kullanıcı öneriyi kabul etti veya reddetti).
     * 
     * @param recommendationId Öneri ID'si
     * @param status Yeni durum ("ACCEPTED", "REJECTED", "VIEWED")
     * @return Güncellenmiş öneri bilgisi
     */
    @PutMapping("/{recommendationId}/status")
    @Operation(summary = "Bir öneri durumunu günceller", 
               description = "Kullanıcının bir öneriyle etkileşimini kaydeder (kabul, red, görüntüleme)")
    public ResponseEntity<RecommendationDTO> updateRecommendationStatus(
            @PathVariable Long recommendationId,
            @RequestParam String status) {
        
        if (!status.equals("ACCEPTED") && !status.equals("REJECTED") && !status.equals("VIEWED")) {
            return ResponseEntity.badRequest().build();
        }
        
        UserRecommendation updatedRecommendation = 
                recommendationService.updateRecommendationStatus(recommendationId, status);
        
        return ResponseEntity.ok(convertToDTO(updatedRecommendation));
    }
    
    /**
     * Yöneticiler için öneri sistemini tetikler ve tüm kullanıcılar için öneriler üretir.
     * 
     * @return Üretilen öneri sayısı
     */
    @PostMapping("/generate")
    @Operation(summary = "Öneri sistemini tetikler", 
               description = "Tüm kullanıcılar için benzerlik hesaplamalarını ve öneri üretimini tetikler (yöneticiler için)")
    public ResponseEntity<Map<String, Integer>> generateRecommendations(
            @RequestParam(defaultValue = "5") int recommendationsPerUser) {
        
        int count = recommendationService.generateRecommendationsForAllUsers(recommendationsPerUser);
        
        return ResponseEntity.ok(Map.of("generatedRecommendations", count));
    }
    
    /**
     * UserRecommendation nesnesini RecommendationDTO'ya dönüştürür.
     * 
     * @param recommendation Dönüştürülecek UserRecommendation nesnesi
     * @return RecommendationDTO nesnesi
     */
    private RecommendationDTO convertToDTO(UserRecommendation recommendation) {
        User recommendedUser = recommendation.getRecommendedUser();
        
        RecommendationDTO dto = new RecommendationDTO();
        dto.setId(recommendation.getId());
        dto.setUserId(recommendation.getTargetUser().getId());
        dto.setRecommendedUserId(recommendedUser.getId());
        dto.setRecommendedUserName(recommendedUser.getName());
        
        // Kullanıcı adını ekleyelim
        if (recommendedUser != null) {
            dto.setRecommendedUserFullName(recommendedUser.getName() != null ? recommendedUser.getName() : "");
            
            // Önerilen kullanıcının tam bilgilerini de ekleyelim (profil resmi dahil)
            UserDTO userDTO = UserDTO.builder()
                    .id(recommendedUser.getId())
                    .name(recommendedUser.getName())
                    .email(recommendedUser.getEmail())
                    .image(recommendedUser.getImage())
                    .build();
            dto.setRecommendedUser(userDTO);
        }
        
        dto.setScore(recommendation.getScore());
        dto.setStatus(recommendation.getStatus() != null ? recommendation.getStatus().name() : "NEW");
        dto.setCreatedAt(recommendation.getCreatedAt() != null ? recommendation.getCreatedAt().getTime() : null);
        
        return dto;
    }
    
    /**
     * UserRecommendation ve RecommendationReason nesnelerini RecommendationDetailDTO'ya dönüştürür.
     * 
     * @param recommendation Dönüştürülecek UserRecommendation nesnesi
     * @param reasons Dönüştürülecek RecommendationReason nesneleri
     * @return RecommendationDetailDTO nesnesi
     */
    private RecommendationDetailDTO convertToDetailDTO(UserRecommendation recommendation, List<RecommendationReason> reasons) {
        RecommendationDetailDTO dto = new RecommendationDetailDTO();
        dto.setRecommendation(convertToDTO(recommendation));
        
        List<RecommendationReasonDTO> reasonDTOs = new ArrayList<>();
        if (reasons != null) {
            reasonDTOs = reasons.stream()
                    .map(this::convertToReasonDTO)
                    .collect(Collectors.toList());
        }
        
        dto.setReasons(reasonDTOs);
        return dto;
    }
    
    /**
     * RecommendationReason nesnesini RecommendationReasonDTO'ya dönüştürür.
     * 
     * @param reason Dönüştürülecek RecommendationReason nesnesi
     * @return RecommendationReasonDTO nesnesi
     */
    private RecommendationReasonDTO convertToReasonDTO(RecommendationReason reason) {
        RecommendationReasonDTO dto = new RecommendationReasonDTO();
        dto.setId(reason.getId());
        dto.setType(reason.getType());
        dto.setDescription(reason.getDescription());
        dto.setScore(reason.getScore());
        
        return dto;
    }
}
