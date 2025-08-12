package com.fuar.recommendation.dto;

import lombok.Data;
import java.util.List;

/**
 * Detaylı öneri için DTO (Data Transfer Object) sınıfı.
 * Öneri ve onun nedenleri hakkında bilgi içerir.
 */
@Data
public class RecommendationDetailDTO {
    
    private RecommendationDTO recommendation;
    private List<RecommendationReasonDTO> reasons;
}
