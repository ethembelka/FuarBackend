package com.fuar.recommendation.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import java.util.Locale;

/**
 * Bu sınıf, öneri nedenleri için metinleri ve açıklamaları temsil eder.
 * Öneri nedenleri, kullanıcı arayüzünde gösterilecek olan açıklamalardır.
 */
@Entity
@Data
@NoArgsConstructor
public class RecommendationReason {
    @Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;           // Veritabanı birincil anahtar
    
    @Column
    private String reasonCode;  // Öneri nedeni kodu (business key)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private UserRecommendation recommendation; // Bağlı olduğu öneri
    
    @Column
    private String type;        // Öneri nedeni tipi
    
    @Column
    private String description; // Açıklama
    
    @Column
    private Double score;       // Öneri puanı
    
    @Column
    private String categoryEn;  // Kategori (İngilizce)
    
    @Column
    private String categoryTr;  // Kategori (Türkçe)
    
    @Column(length = 1000)
    private String templateEn;  // Şablon (İngilizce)
    
    @Column(length = 1000)
    private String templateTr;  // Şablon (Türkçe)
    
    @Column
    private Double weight;  // Ağırlık (öneri sıralamasında kullanılır)
    
    /**
     * Verilen dil için şablonu döndürür.
     * 
     * @param locale Dil
     * @return Şablon
     */
    public String getTemplate(Locale locale) {
        if (locale.getLanguage().equals("tr")) {
            return templateTr;
        }
        return templateEn;
    }
    
    /**
     * Verilen dil için kategoriyi döndürür.
     * 
     * @param locale Dil
     * @return Kategori
     */
    public String getCategory(Locale locale) {
        if (locale.getLanguage().equals("tr")) {
            return categoryTr;
        }
        return categoryEn;
    }
}
