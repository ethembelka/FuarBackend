package com.fuar.recommendation.repository;

import com.fuar.recommendation.model.RecommendationReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RecommendationReason varlığı için veri erişim katmanı.
 */
@Repository
public interface RecommendationReasonRepository extends JpaRepository<RecommendationReason, String> {
    
    /**
     * Belirli bir kategoriye ait öneri nedenlerini bulur.
     * 
     * @param categoryEn Kategori (İngilizce)
     * @return Belirtilen kategoriye ait öneri nedenleri
     */
    List<RecommendationReason> findByCategoryEnOrderByWeightDesc(String categoryEn);
    
    /**
     * Belirli bir kategoriye ait öneri nedenlerini bulur.
     * 
     * @param categoryTr Kategori (Türkçe)
     * @return Belirtilen kategoriye ait öneri nedenleri
     */
    List<RecommendationReason> findByCategoryTrOrderByWeightDesc(String categoryTr);
    
    /**
     * Belirli bir öneriye ait öneri nedenlerini bulur.
     * 
     * @param recommendation Öneri
     * @return Belirtilen öneriye ait öneri nedenleri
     */
    List<RecommendationReason> findByRecommendation(com.fuar.recommendation.model.UserRecommendation recommendation);
}
