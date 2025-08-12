package com.fuar.recommendation.service;

import com.fuar.model.User;
import com.fuar.recommendation.model.RecommendationReason;
import com.fuar.recommendation.model.UserRecommendation;
import com.fuar.recommendation.model.UserSimilarity;
import com.fuar.recommendation.repository.RecommendationReasonRepository;
import com.fuar.recommendation.repository.UserRecommendationRepository;
import com.fuar.recommendation.repository.UserSimilarityRepository;
import com.fuar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bu servis, kullanıcı önerilerini oluşturmak ve yönetmekten sorumludur.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {

    private final UserRepository userRepository;
    private final UserSimilarityRepository similarityRepository;
    private final UserRecommendationRepository recommendationRepository;
    private final RecommendationReasonRepository reasonRepository;
    private final SimilarityService similarityService;
    private final FeatureExtractionService featureExtractionService;

    /**
     * Belirli bir kullanıcı için öneriler oluşturur.
     * 
     * @param userId Kullanıcı ID'si
     * @param count Öneri sayısı
     * @return Oluşturulan öneri sayısı
     */
    @Transactional
    public int generateRecommendationsForUser(Long userId, int count) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Öncelikle kullanıcının özellik vektörünü güncelle
        featureExtractionService.extractFeaturesForUser(userId);
        
        // Kullanıcının benzerliklerini hesapla
        similarityService.computeAndStoreSimilaritiesForUser(userId);
        
        // Mevcut önerileri temizle
        recommendationRepository.deleteByUser(user);
        
        // En benzer kullanıcıları bul
        List<UserSimilarity> similarities = similarityService.findMostSimilarUsers(userId, count * 2);
        
        int generatedCount = 0;
        
        for (UserSimilarity similarity : similarities) {
            if (generatedCount >= count) {
                break;
            }
            
            // Düşük benzerlik skoruna sahip kullanıcıları atlayalım
            if (similarity.getSimilarityScore() < 0.1) {
                continue;
            }
            
            try {
                // Yeni bir öneri oluştur
                UserRecommendation recommendation = new UserRecommendation();
                recommendation.setUser(user);
                recommendation.setRecommendedUser(similarity.getUser2());
                recommendation.setScore(similarity.getSimilarityScore());
                recommendation.setStatus(UserRecommendation.RecommendationStatus.NEW);
                recommendation.setCreatedAt(new Date(System.currentTimeMillis()));
                recommendation.setLastUpdated(new Date(System.currentTimeMillis()));
                
                // Öneriyi kaydet
                UserRecommendation savedRecommendation = recommendationRepository.save(recommendation);
                
                // Öneri nedenleri oluştur
                generateRecommendationReasons(savedRecommendation);
                
                generatedCount++;
            } catch (Exception e) {
                log.error("Error generating recommendation for user {} with similar user {}", 
                        userId, similarity.getUser2().getId(), e);
            }
        }
        
        return generatedCount;
    }
    
    /**
     * Bir öneri için nedenler oluşturur.
     * 
     * @param recommendation Öneri nesnesi
     */
    private void generateRecommendationReasons(UserRecommendation recommendation) {
        try {
            User user1 = recommendation.getUser();
            User user2 = recommendation.getRecommendedUser();
            
            // Kullanıcıların özellik vektörlerini al
            var vector1 = featureExtractionService.getUserFeatureVector(user1.getId());
            var vector2 = featureExtractionService.getUserFeatureVector(user2.getId());
            
            if (vector1 == null || vector2 == null) {
                return;
            }
            
            // Ortak becerileri bul
            List<String> commonSkills = findCommonFeatures(vector1.getSkillWeights(), vector2.getSkillWeights(), 3);
            if (!commonSkills.isEmpty()) {
                try {
                    RecommendationReason reason = new RecommendationReason();
                    reason.setRecommendation(recommendation);
                    reason.setReasonCode("COMMON_SKILLS");
                    reason.setType("COMMON_SKILLS");
                    reason.setDescription("Ortak beceriler: " + String.join(", ", commonSkills));
                    reason.setScore(0.3); // Beceriler için yüksek ağırlık
                    reasonRepository.save(reason);
                } catch (Exception e) {
                    log.error("Error saving COMMON_SKILLS reason for recommendation {}: {}", recommendation.getId(), e.getMessage());
                }
            }
            
            // Ortak sektörleri bul
            List<String> commonSectors = findCommonFeatures(vector1.getSectorWeights(), vector2.getSectorWeights(), 2);
            if (!commonSectors.isEmpty()) {
                try {
                    RecommendationReason reason = new RecommendationReason();
                    reason.setRecommendation(recommendation);
                    reason.setReasonCode("COMMON_SECTORS");
                    reason.setType("COMMON_SECTORS");
                    reason.setDescription("Ortak sektörler: " + String.join(", ", commonSectors));
                    reason.setScore(0.25);
                    reasonRepository.save(reason);
                } catch (Exception e) {
                    log.error("Error saving COMMON_SECTORS reason for recommendation {}: {}", recommendation.getId(), e.getMessage());
                }
            }
            
            // Ortak uzmanlık alanlarını bul
            List<String> commonExpertise = findCommonFeatures(vector1.getExpertiseWeights(), vector2.getExpertiseWeights(), 2);
            if (!commonExpertise.isEmpty()) {
                try {
                    RecommendationReason reason = new RecommendationReason();
                    reason.setRecommendation(recommendation);
                    reason.setReasonCode("COMMON_EXPERTISE");
                    reason.setType("COMMON_EXPERTISE");
                    reason.setDescription("Ortak uzmanlık alanları: " + String.join(", ", commonExpertise));
                    reason.setScore(0.25);
                    reasonRepository.save(reason);
                } catch (Exception e) {
                    log.error("Error saving COMMON_EXPERTISE reason for recommendation {}: {}", recommendation.getId(), e.getMessage());
                }
            }
            
            // Ortak ilgi alanlarını bul
            List<String> commonInterests = findCommonFeatures(vector1.getInterestWeights(), vector2.getInterestWeights(), 3);
            if (!commonInterests.isEmpty()) {
                try {
                    RecommendationReason reason = new RecommendationReason();
                    reason.setRecommendation(recommendation);
                    reason.setReasonCode("COMMON_INTERESTS");
                    reason.setType("COMMON_INTERESTS");
                    reason.setDescription("Ortak ilgi alanları: " + String.join(", ", commonInterests));
                    reason.setScore(0.2);
                    reasonRepository.save(reason);
                } catch (Exception e) {
                    log.error("Error saving COMMON_INTERESTS reason for recommendation {}: {}", recommendation.getId(), e.getMessage());
                }
            }
            
            // Ortak eğitim alanlarını bul
            List<String> commonEducation = findCommonFeatures(vector1.getEducationFieldWeights(), vector2.getEducationFieldWeights(), 2);
            if (!commonEducation.isEmpty()) {
                try {
                    RecommendationReason reason = new RecommendationReason();
                    reason.setRecommendation(recommendation);
                    reason.setReasonCode("COMMON_EDUCATION");
                    reason.setType("COMMON_EDUCATION");
                    reason.setDescription("Ortak eğitim alanları: " + String.join(", ", commonEducation));
                    reason.setScore(0.15);
                    reasonRepository.save(reason);
                } catch (Exception e) {
                    log.error("Error saving COMMON_EDUCATION reason for recommendation {}: {}", recommendation.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in generateRecommendationReasons for recommendation {}: {}", recommendation.getId(), e.getMessage());
        }
    }
    
    /**
     * İki özellik vektörü arasındaki ortak özellikleri bulur.
     * 
     * @param features1 Birinci özellik vektörü
     * @param features2 İkinci özellik vektörü
     * @param limit Sonuç sayısı sınırı
     * @return Ortak özelliklerin listesi
     */
    private List<String> findCommonFeatures(Map<String, Double> features1, Map<String, Double> features2, int limit) {
        if (features1 == null || features2 == null || features1.isEmpty() || features2.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Ortak anahtarları bul
        Set<String> commonKeys = new HashSet<>(features1.keySet());
        commonKeys.retainAll(features2.keySet());
        
        // Ortak anahtarları ağırlığa göre sırala
        return commonKeys.stream()
                .sorted((k1, k2) -> {
                    double w1 = features1.get(k1) * features2.get(k1);
                    double w2 = features1.get(k2) * features2.get(k2);
                    return Double.compare(w2, w1); // Azalan sıralama
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Tüm kullanıcılar için öneriler oluşturur.
     * 
     * @param recommendationsPerUser Her kullanıcı için oluşturulacak öneri sayısı
     * @return Toplam oluşturulan öneri sayısı
     */
    @Transactional
    public int generateRecommendationsForAllUsers(int recommendationsPerUser) {
        // Öncelikle tüm kullanıcılar için özellik vektörlerini ve benzerliklerini güncelle
        featureExtractionService.extractFeaturesForAllUsers();
        similarityService.computeAndStoreAllSimilarities();
        
        List<User> allUsers = userRepository.findAll();
        int totalCount = 0;
        
        // Her kullanıcı için öneriler oluştur
        for (User user : allUsers) {
            try {
                int count = generateRecommendationsForUser(user.getId(), recommendationsPerUser);
                totalCount += count;
            } catch (Exception e) {
                log.error("Error generating recommendations for user: {}", user.getId(), e);
            }
        }
        
        return totalCount;
    }
    
    /**
     * Belirli bir kullanıcı için önerileri getirir.
     * 
     * @param userId Kullanıcı ID'si
     * @param limit Sonuç sayısı sınırı
     * @return Öneriler listesi
     */
    @Transactional
    public List<UserRecommendation> getRecommendationsForUser(Long userId, int limit) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }
        
        // Önerileri getir ve skor sırasına göre sırala
        List<UserRecommendation> recommendations = recommendationRepository.findByUserOrderByScoreDesc(user)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        // Eğer öneri yoksa veya yeterli öneri yoksa, yeni öneriler oluştur
        if (recommendations.size() < limit) {
            generateRecommendationsForUser(userId, limit);
            
            // Yeniden önerileri getir
            recommendations = recommendationRepository.findByUserOrderByScoreDesc(user)
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        
        return recommendations;
    }
    
    /**
     * Belirli bir öneri için nedenler listesini getirir.
     * 
     * @param recommendationId Öneri ID'si
     * @return Nedenler listesi
     */
    public List<RecommendationReason> getReasonsForRecommendation(Long recommendationId) {
        UserRecommendation recommendation = recommendationRepository.findById(recommendationId).orElse(null);
        if (recommendation == null) {
            return Collections.emptyList();
        }
        
        return reasonRepository.findByRecommendation(recommendation);
    }
    
    /**
     * Belirli bir kullanıcı için tüm önerileri ve nedenlerini getirir.
     * 
     * @param userId Kullanıcı ID'si
     * @param limit Sonuç sayısı sınırı
     * @return Öneriler ve nedenlerin listesi (öneri ID -> nedenler listesi)
     */
    @Transactional
    public Map<UserRecommendation, List<RecommendationReason>> getRecommendationsWithReasonsForUser(Long userId, int limit) {
        List<UserRecommendation> recommendations = getRecommendationsForUser(userId, limit);
        
        Map<UserRecommendation, List<RecommendationReason>> result = new LinkedHashMap<>();
        
        for (UserRecommendation recommendation : recommendations) {
            List<RecommendationReason> reasons = reasonRepository.findByRecommendation(recommendation);
            result.put(recommendation, reasons);
        }
        
        return result;
    }
    
    /**
     * Belirli bir öneriyi kullanıcının etkileşimine göre günceller.
     * 
     * @param recommendationId Öneri ID'si
     * @param status Yeni durum ("ACCEPTED", "REJECTED", "VIEWED")
     * @return Güncellenmiş öneri nesnesi
     */
    @Transactional
    public UserRecommendation updateRecommendationStatus(Long recommendationId, String statusStr) {
        UserRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new RuntimeException("Recommendation not found with id: " + recommendationId));
        
        try {
            UserRecommendation.RecommendationStatus status = UserRecommendation.RecommendationStatus.valueOf(statusStr);
            recommendation.setStatus(status);
            recommendation.setLastUpdated(new Date(System.currentTimeMillis()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid recommendation status: " + statusStr);
        }
        
        return recommendationRepository.save(recommendation);
    }
}
