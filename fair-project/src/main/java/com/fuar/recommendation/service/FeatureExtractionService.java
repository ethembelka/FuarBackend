package com.fuar.recommendation.service;

import com.fuar.model.Education;
import com.fuar.model.Skill;
import com.fuar.model.User;
import com.fuar.model.UserInfo;
import com.fuar.model.WorkExperience;
import com.fuar.model.Publication;
import com.fuar.recommendation.model.UserFeatureVector;
import com.fuar.recommendation.repository.UserFeatureVectorRepository;
import com.fuar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Bu servis, kullanıcı profillerinden özellik vektörleri oluşturmaktan sorumludur.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FeatureExtractionService {
    
    private final UserRepository userRepository;
    private final UserFeatureVectorRepository userFeatureVectorRepository;
    
    /**
     * Belirli bir kullanıcı için özellik vektörü oluşturur veya günceller.
     * 
     * @param userId Kullanıcı ID'si
     * @return Oluşturulan veya güncellenen özellik vektörü
     */
    @Transactional
    public UserFeatureVector extractFeaturesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Kullanıcının mevcut özellik vektörünü bul veya yeni oluştur
        UserFeatureVector featureVector = userFeatureVectorRepository.findByUser(user)
                .orElse(new UserFeatureVector(user));
        
        // Becerileri çıkar
        extractSkillFeatures(user, featureVector);
        
        // Sektörleri çıkar
        extractSectorFeatures(user, featureVector);
        
        // Uzmanlık alanlarını çıkar
        extractExpertiseFeatures(user, featureVector);
        
        // İlgi alanlarını çıkar
        extractInterestFeatures(user, featureVector);
        
        // Eğitim alanlarını çıkar
        extractEducationFeatures(user, featureVector);
        
        // Son güncelleme zamanını ayarla
        featureVector.setLastUpdated(System.currentTimeMillis());
        
        // Özellik vektörünü kaydet ve döndür
        return userFeatureVectorRepository.save(featureVector);
    }
    
    /**
     * Tüm kullanıcılar için özellik vektörleri oluşturur.
     * 
     * @return Oluşturulan özellik vektörleri sayısı
     */
    @Transactional
    public int extractFeaturesForAllUsers() {
        int count = 0;
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            try {
                extractFeaturesForUser(user.getId());
                count++;
            } catch (Exception e) {
                log.error("Error extracting features for user: {}", user.getId(), e);
            }
        }
        
        return count;
    }
    
    /**
     * Kullanıcının becerilerinden özellikler çıkarır.
     * 
     * @param user Kullanıcı
     * @param featureVector Güncellenecek özellik vektörü
     */
    private void extractSkillFeatures(User user, UserFeatureVector featureVector) {
        Map<String, Double> skillWeights = new HashMap<>();
        
        if (user.getUserInfo() != null && user.getUserInfo().getSkills() != null) {
            Set<Skill> skills = user.getUserInfo().getSkills();
            
            // Basit bir yaklaşım: Her beceriye eşit ağırlık ver
            double weight = skills.isEmpty() ? 0.0 : 1.0 / skills.size();
            
            for (Skill skill : skills) {
                if (skill.getName() != null && !skill.getName().trim().isEmpty()) {
                    skillWeights.put(skill.getName().toLowerCase(), weight);
                }
            }
        }
        
        featureVector.setSkillWeights(skillWeights);
    }
    
    /**
     * Kullanıcının iş deneyimlerinden sektör özellikleri çıkarır.
     * 
     * @param user Kullanıcı
     * @param featureVector Güncellenecek özellik vektörü
     */
    private void extractSectorFeatures(User user, UserFeatureVector featureVector) {
        Map<String, Double> sectorWeights = new HashMap<>();
        
        if (user.getUserInfo() != null && user.getUserInfo().getWorkExperiences() != null) {
            List<WorkExperience> experiences = com.fuar.recommendation.util.CollectionUtils.toList(user.getUserInfo().getWorkExperiences());
            
            // Sektörleri toplayıp frekanslarını hesapla
            Map<String, Integer> sectorCounts = new HashMap<>();
            
            for (WorkExperience exp : experiences) {
                if (exp.getSector() != null && !exp.getSector().trim().isEmpty()) {
                    String sector = exp.getSector().toLowerCase();
                    sectorCounts.put(sector, sectorCounts.getOrDefault(sector, 0) + 1);
                }
            }
            
            // Frekansları ağırlıklara dönüştür
            int totalCount = sectorCounts.values().stream().mapToInt(Integer::intValue).sum();
            
            for (Map.Entry<String, Integer> entry : sectorCounts.entrySet()) {
                sectorWeights.put(entry.getKey(), (double) entry.getValue() / totalCount);
            }
        }
        
        featureVector.setSectorWeights(sectorWeights);
    }
    
    /**
     * Kullanıcının uzmanlık alanlarından özellikler çıkarır.
     * 
     * @param user Kullanıcı
     * @param featureVector Güncellenecek özellik vektörü
     */
    private void extractExpertiseFeatures(User user, UserFeatureVector featureVector) {
        Map<String, Double> expertiseWeights = new HashMap<>();
        
        // Burada becerilerden, çalışma deneyiminden ve yayınlardan uzmanlık alanları çıkarılabilir
        // Şimdilik basit bir yaklaşım kullanıyoruz
        
        if (user.getUserInfo() != null) {
            UserInfo userInfo = user.getUserInfo();
            Set<String> expertiseAreas = new HashSet<>();
            
            // Becerilerden uzmanlık alanları çıkar
            if (userInfo.getSkills() != null) {
                expertiseAreas.addAll(userInfo.getSkills().stream()
                        .map(Skill::getName)
                        .filter(name -> name != null && !name.trim().isEmpty())
                        .collect(Collectors.toSet()));
            }
            
            // İş deneyimlerinden pozisyonlara göre uzmanlık alanları çıkar
            if (userInfo.getWorkExperiences() != null) {
                expertiseAreas.addAll(userInfo.getWorkExperiences().stream()
                        .map(WorkExperience::getPosition)
                        .filter(pos -> pos != null && !pos.trim().isEmpty())
                        .collect(Collectors.toSet()));
            }
            
            // Yayınlardan uzmanlık alanları çıkar
            if (userInfo.getPublications() != null) {
                expertiseAreas.addAll(userInfo.getPublications().stream()
                        .map(pub -> pub.getTopic())
                        .filter(topic -> topic != null && !topic.trim().isEmpty())
                        .collect(Collectors.toSet()));
            }
            
            // Her uzmanlık alanına eşit ağırlık ver
            double weight = expertiseAreas.isEmpty() ? 0.0 : 1.0 / expertiseAreas.size();
            
            for (String expertise : expertiseAreas) {
                expertiseWeights.put(expertise.toLowerCase(), weight);
            }
        }
        
        featureVector.setExpertiseWeights(expertiseWeights);
    }
    
    /**
     * Kullanıcının ilgi alanlarından özellikler çıkarır.
     * 
     * @param user Kullanıcı
     * @param featureVector Güncellenecek özellik vektörü
     */
    private void extractInterestFeatures(User user, UserFeatureVector featureVector) {
        Map<String, Double> interestWeights = new HashMap<>();
        
        // Şimdilik basit bir yaklaşım kullanıyoruz
        // Gerçek bir uygulamada, ilgi alanları kullanıcı etkileşimleri (etkinliklere katılım, profil ziyaretleri vb.) 
        // gibi kaynaklardan çıkarılabilir
        
        // Şimdilik, becerilerden ve eğitimlerden ilgi alanları çıkaralım
        if (user.getUserInfo() != null) {
            UserInfo userInfo = user.getUserInfo();
            Set<String> interests = new HashSet<>();
            
            // Becerilerden ilgi alanları çıkar
            if (userInfo.getSkills() != null) {
                interests.addAll(userInfo.getSkills().stream()
                        .map(Skill::getName)
                        .filter(name -> name != null && !name.trim().isEmpty())
                        .collect(Collectors.toSet()));
            }
            
                        // Eğitimlerden alan bilgisini çıkar
            if (userInfo.getEducations() != null) {
                interests.addAll(userInfo.getEducations().stream()
                        .map(edu -> edu.getMajor())
                        .filter(major -> major != null && !major.trim().isEmpty())
                        .collect(Collectors.toSet()));
            }
            
            // Her ilgi alanına eşit ağırlık ver
            double weight = interests.isEmpty() ? 0.0 : 1.0 / interests.size();
            
            for (String interest : interests) {
                interestWeights.put(interest.toLowerCase(), weight);
            }
        }
        
        featureVector.setInterestWeights(interestWeights);
    }
    
    /**
     * Kullanıcının eğitim bilgilerinden özellikler çıkarır.
     * 
     * @param user Kullanıcı
     * @param featureVector Güncellenecek özellik vektörü
     */
    private void extractEducationFeatures(User user, UserFeatureVector featureVector) {
        Map<String, Double> educationFieldWeights = new HashMap<>();
        
        if (user.getUserInfo() != null && user.getUserInfo().getEducations() != null) {
            List<Education> educations = com.fuar.recommendation.util.CollectionUtils.toList(user.getUserInfo().getEducations());
            
            // Eğitim alanlarını toplayıp frekanslarını hesapla
            Map<String, Integer> fieldCounts = new HashMap<>();
            
            for (Education edu : educations) {
                if (edu.getMajor() != null && !edu.getMajor().trim().isEmpty()) {
                    String field = edu.getMajor().toLowerCase();
                    fieldCounts.put(field, fieldCounts.getOrDefault(field, 0) + 1);
                }
                if (edu.getDegree() != null && !edu.getDegree().trim().isEmpty()) {
                    String degree = edu.getDegree().toLowerCase();
                    fieldCounts.put(degree, fieldCounts.getOrDefault(degree, 0) + 1);
                }
            }
            
            // Frekansları ağırlıklara dönüştür
            int totalCount = fieldCounts.values().stream().mapToInt(Integer::intValue).sum();
            
            if (totalCount > 0) {
                for (Map.Entry<String, Integer> entry : fieldCounts.entrySet()) {
                    educationFieldWeights.put(entry.getKey(), (double) entry.getValue() / totalCount);
                }
            }
        }
        
        featureVector.setEducationFieldWeights(educationFieldWeights);
    }
    
    /**
     * Kullanıcı özellik vektörünü getirir.
     * 
     * @param userId Kullanıcı ID'si
     * @return Kullanıcının özellik vektörü, yoksa null
     */
    public UserFeatureVector getUserFeatureVector(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        
        Optional<UserFeatureVector> featureVector = userFeatureVectorRepository.findByUser(user);
        
        if (featureVector.isEmpty()) {
            // Özellik vektörü yoksa, oluştur
            return extractFeaturesForUser(userId);
        }
        
        return featureVector.get();
    }
}
