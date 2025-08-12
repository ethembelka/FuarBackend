package com.fuar.recommendation.dto;

import lombok.Data;

/**
 * Öneri nedeni için DTO (Data Transfer Object) sınıfı.
 */
@Data
public class RecommendationReasonDTO {
    
    private Long id;
    private String type;
    private String description;
    private Double score;
}
