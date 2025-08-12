package com.fuar.recommendation.dto;

import com.fuar.dto.UserDTO;
import lombok.Data;

/**
 * Öneri için DTO (Data Transfer Object) sınıfı.
 */
@Data
public class RecommendationDTO {
    
    private Long id;
    private Long userId;
    private Long recommendedUserId;
    private String recommendedUserName;
    private String recommendedUserFullName;
    private Double score;
    private String status;
    private Long createdAt;
    private UserDTO recommendedUser; // Önerilen kullanıcının tam bilgileri (image dahil)
}
