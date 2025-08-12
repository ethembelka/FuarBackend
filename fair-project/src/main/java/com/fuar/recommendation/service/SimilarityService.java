package com.fuar.recommendation.service;

import com.fuar.model.User;
import com.fuar.recommendation.model.UserFeatureVector;
import com.fuar.recommendation.model.UserSimilarity;
import com.fuar.recommendation.repository.UserFeatureVectorRepository;
import com.fuar.recommendation.repository.UserSimilarityRepository;
import com.fuar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bu servis, kullanıcılar arasındaki benzerlikleri hesaplamaktan sorumludur.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SimilarityService {

    private final UserRepository userRepository;
    private final UserFeatureVectorRepository featureVectorRepository;
    private final UserSimilarityRepository userSimilarityRepository;
    private final FeatureExtractionService featureExtractionService;

    /**
     * İki kullanıcı arasındaki benzerliği hesaplar.
     * 
     * @param user1Id İlk kullanıcı ID'si
     * @param user2Id İkinci kullanıcı ID'si
     * @return Hesaplanan benzerlik skoru
     */
    public double calculateSimilarity(Long user1Id, Long user2Id) {
        // Her iki kullanıcı için özellik vektörlerini al
        UserFeatureVector vector1 = featureExtractionService.getUserFeatureVector(user1Id);
        UserFeatureVector vector2 = featureExtractionService.getUserFeatureVector(user2Id);
        
        if (vector1 == null || vector2 == null) {
            return 0.0;
        }
        
        // Benzerlik skorunu hesapla
        double skillSimilarity = calculateCosineSimilarity(vector1.getSkillWeights(), vector2.getSkillWeights());
        double sectorSimilarity = calculateCosineSimilarity(vector1.getSectorWeights(), vector2.getSectorWeights());
        double expertiseSimilarity = calculateCosineSimilarity(vector1.getExpertiseWeights(), vector2.getExpertiseWeights());
        double interestSimilarity = calculateCosineSimilarity(vector1.getInterestWeights(), vector2.getInterestWeights());
        double educationSimilarity = calculateCosineSimilarity(vector1.getEducationFieldWeights(), vector2.getEducationFieldWeights());
        
        // Farklı özelliklerin ağırlıklı ortalamasını hesapla
        // Burada farklı özelliklere farklı ağırlıklar verilebilir
        double weightedSimilarity = (
            skillSimilarity * 0.3 +
            sectorSimilarity * 0.2 +
            expertiseSimilarity * 0.2 +
            interestSimilarity * 0.15 +
            educationSimilarity * 0.15
        );
        
        return weightedSimilarity;
    }
    
    /**
     * İki özellik vektörü arasındaki kosinüs benzerliğini hesaplar.
     * 
     * @param vector1 Birinci özellik vektörü (anahtar-ağırlık eşlemeleri)
     * @param vector2 İkinci özellik vektörü (anahtar-ağırlık eşlemeleri)
     * @return Kosinüs benzerlik skoru (0-1 arasında)
     */
    private double calculateCosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }
        
        // Ortak anahtarları bul
        Set<String> commonKeys = new HashSet<>(vector1.keySet());
        commonKeys.retainAll(vector2.keySet());
        
        if (commonKeys.isEmpty()) {
            return 0.0;
        }
        
        // Dot product hesapla
        double dotProduct = 0.0;
        for (String key : commonKeys) {
            dotProduct += vector1.get(key) * vector2.get(key);
        }
        
        // Vektör boyutlarını hesapla
        double magnitude1 = Math.sqrt(
            vector1.values().stream().mapToDouble(value -> value * value).sum()
        );
        
        double magnitude2 = Math.sqrt(
            vector2.values().stream().mapToDouble(value -> value * value).sum()
        );
        
        // Kosinüs benzerliğini hesapla ve döndür
        if (magnitude1 > 0 && magnitude2 > 0) {
            return dotProduct / (magnitude1 * magnitude2);
        } else {
            return 0.0;
        }
    }
    
    /**
     * Belirli bir kullanıcı için tüm kullanıcılarla benzerlik skorlarını hesaplar ve veritabanına kaydeder.
     * 
     * @param userId Kullanıcı ID'si
     * @return Hesaplanan benzerlik kayıtları sayısı
     */
    @Transactional
    public int computeAndStoreSimilaritiesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        List<User> allUsers = userRepository.findAll();
        int count = 0;
        
        // Mevcut benzerlik kayıtlarını temizle
        userSimilarityRepository.deleteByUser1(user);
        
        for (User otherUser : allUsers) {
            // Kendisiyle benzerlik hesaplama
            if (otherUser.getId().equals(userId)) {
                continue;
            }
            
            try {
                // Benzerlik skorunu hesapla
                double similarityScore = calculateSimilarity(userId, otherUser.getId());
                
                // Benzerlik skorunu veritabanına kaydet
                UserSimilarity similarity = new UserSimilarity();
                similarity.setUser1(user);
                similarity.setUser2(otherUser);
                similarity.setSimilarityScore(similarityScore);
                similarity.setLastUpdated(new Date(System.currentTimeMillis()));
                
                userSimilarityRepository.save(similarity);
                count++;
            } catch (Exception e) {
                log.error("Error computing similarity between users {} and {}", userId, otherUser.getId(), e);
            }
        }
        
        return count;
    }
    
    /**
     * Tüm kullanıcılar için benzerlik skorlarını hesaplar ve veritabanına kaydeder.
     * 
     * @return Hesaplanan benzerlik kayıtları sayısı
     */
    @Transactional
    public int computeAndStoreAllSimilarities() {
        // Önce tüm kullanıcılar için özellik vektörlerini oluştur
        featureExtractionService.extractFeaturesForAllUsers();
        
        List<User> allUsers = userRepository.findAll();
        int totalCount = 0;
        
        // Mevcut tüm benzerlik kayıtlarını temizle
        userSimilarityRepository.deleteAll();
        
        // Her kullanıcı çifti için benzerlik hesapla
        for (int i = 0; i < allUsers.size(); i++) {
            User user1 = allUsers.get(i);
            
            for (int j = i + 1; j < allUsers.size(); j++) {
                User user2 = allUsers.get(j);
                
                try {
                    // Benzerlik skorunu hesapla
                    double similarityScore = calculateSimilarity(user1.getId(), user2.getId());
                    
                    // İlk kullanıcıdan ikinci kullanıcıya benzerlik
                    UserSimilarity similarity1 = new UserSimilarity();
                    similarity1.setUser1(user1);
                    similarity1.setUser2(user2);
                    similarity1.setSimilarityScore(similarityScore);
                    similarity1.setLastUpdated(new Date(System.currentTimeMillis()));
                    userSimilarityRepository.save(similarity1);
                    
                    // İkinci kullanıcıdan ilk kullanıcıya benzerlik
                    UserSimilarity similarity2 = new UserSimilarity();
                    similarity2.setUser1(user2);
                    similarity2.setUser2(user1);
                    similarity2.setSimilarityScore(similarityScore);
                    similarity2.setLastUpdated(new Date(System.currentTimeMillis()));
                    userSimilarityRepository.save(similarity2);
                    
                    totalCount += 2;
                } catch (Exception e) {
                    log.error("Error computing similarity between users {} and {}", user1.getId(), user2.getId(), e);
                }
            }
        }
        
        return totalCount;
    }
    
    /**
     * Belirli bir kullanıcı için en benzer kullanıcıları bulur.
     * 
     * @param userId Kullanıcı ID'si
     * @param limit Sonuç sayısı sınırı
     * @return Benzerlik skoru yüksek olan kullanıcıların listesi
     */
    public List<UserSimilarity> findMostSimilarUsers(Long userId, int limit) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }
        
        return userSimilarityRepository.findByUser1OrderBySimilarityScoreDesc(user)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
