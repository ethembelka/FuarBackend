package com.fuar.recommendation.model;

import com.fuar.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import java.util.Date;

/**
 * Bu sınıf, kullanıcılar arasındaki benzerlik skorlarını temsil eder.
 */
@Entity
@Data
@NoArgsConstructor
public class UserSimilarity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private User user1;
    
    @ManyToOne
    private User user2;
    
    // Genel benzerlik skoru (0-1 arasında)
    @Column
    private Double similarityScore;
    
    // Beceriler açısından benzerlik (0-1 arasında)
    @Column
    private Double skillSimilarity;
    
    // Sektörler açısından benzerlik (0-1 arasında)
    @Column
    private Double sectorSimilarity;
    
    // Uzmanlık alanları açısından benzerlik (0-1 arasında)
    @Column
    private Double expertiseSimilarity;
    
    // İlgi alanları açısından benzerlik (0-1 arasında)
    @Column
    private Double interestSimilarity;
    
    // Eğitim alanları açısından benzerlik (0-1 arasında)
    @Column
    private Double educationSimilarity;
    
    // En güçlü benzerlik nedeni (recommendation_reason tablosundaki ID'ye referans)
    @Column
    private String primaryReasonCode;
    
    // Son güncelleme zamanı
    @Column
    private Date lastUpdated;
    
    public UserSimilarity(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.lastUpdated = new Date();
    }
}
