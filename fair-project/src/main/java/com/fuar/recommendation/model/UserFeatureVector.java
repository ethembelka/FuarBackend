package com.fuar.recommendation.model;

import com.fuar.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Bu sınıf, bir kullanıcının özellik vektörünü temsil eder.
 * Özellik vektörü, kullanıcının profilinden çıkarılan özellikleri içerir.
 */
@Entity
@Data
@NoArgsConstructor
public class UserFeatureVector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    private User user;
    
    // Kullanıcının becerileri ve her becerinin ağırlığı (0-1 arasında)
    @ElementCollection
    private Map<String, Double> skillWeights = new HashMap<>();
    
    // Kullanıcının ilgilendiği sektörler ve her sektörün ağırlığı (0-1 arasında)
    @ElementCollection
    private Map<String, Double> sectorWeights = new HashMap<>();
    
    // Kullanıcının uzmanlık alanları ve her alanın ağırlığı (0-1 arasında)
    @ElementCollection
    private Map<String, Double> expertiseWeights = new HashMap<>();
    
    // Genel ilgi alanları
    @ElementCollection
    private Map<String, Double> interestWeights = new HashMap<>();
    
    // Eğitim alanları
    @ElementCollection
    private Map<String, Double> educationFieldWeights = new HashMap<>();
    
    // Son güncelleme zamanı
    @Column
    private long lastUpdated;
    
    public UserFeatureVector(User user) {
        this.user = user;
        this.lastUpdated = System.currentTimeMillis();
    }
}
