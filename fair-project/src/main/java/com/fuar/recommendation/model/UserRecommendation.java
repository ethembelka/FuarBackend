package com.fuar.recommendation.model;

import com.fuar.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import java.util.Date;
import java.util.List;

/**
 * Bu sınıf, bir kullanıcıya yapılan önerileri temsil eder.
 */
@Entity
@Data
@NoArgsConstructor
public class UserRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private User targetUser;  // Önerinin yapıldığı kullanıcı
    
    @ManyToOne
    private User recommendedUser;  // Önerilen kullanıcı
    
    @Column
    private Double score;  // Öneri puanı
    
    @Column
    private String reasonCode;  // Öneri nedeni kodu
    
    @Column(length = 1000)
    private String reasonDescription;  // Öneri nedeni açıklaması
    
    @Column
    @Enumerated(EnumType.STRING)
    private RecommendationType recommendationType;  // Öneri türü
    
    @Column
    private Date createdAt;  // Oluşturulma zamanı
    
    @Column
    private Date seenAt;  // Görülme zamanı
    
    @Column
    private Date lastUpdated;  // Son güncelleme zamanı
    
    @Column
    private Boolean clicked;  // Tıklanma durumu
    
    @Column
    @Enumerated(EnumType.STRING)
    private RecommendationStatus status = RecommendationStatus.NEW;  // Öneri durumu
    
    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RecommendationReason> reasons;  // Bu öneriye ait nedenler
    
    public enum RecommendationStatus {
        NEW,        // Yeni öneri
        VIEWED,     // Görülmüş öneri
        ACCEPTED,   // Kabul edilmiş öneri
        REJECTED    // Reddedilmiş öneri
    }
    
    public enum RecommendationType {
        SIMILAR_SKILLS,
        SIMILAR_SECTOR,
        SIMILAR_EXPERTISE,
        SIMILAR_INTERESTS,
        SIMILAR_EDUCATION,
        POTENTIAL_MENTOR,
        POTENTIAL_MENTEE,
        BUSINESS_OPPORTUNITY,
        RESEARCH_COLLABORATION
    }
    
    public UserRecommendation(User targetUser, User recommendedUser) {
        this.targetUser = targetUser;
        this.recommendedUser = recommendedUser;
        this.createdAt = new Date();
        this.clicked = false;
        this.lastUpdated = new Date();
    }
    
    // Geriye dönük uyumluluk için targetUser -> user alias metodları
    public User getUser() {
        return this.targetUser;
    }
    
    public void setUser(User user) {
        this.targetUser = user;
    }
}
