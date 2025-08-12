package com.fuar.recommendation.repository;

import com.fuar.model.User;
import com.fuar.recommendation.model.UserSimilarity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserSimilarity varlığı için veri erişim katmanı.
 */
@Repository
public interface UserSimilarityRepository extends JpaRepository<UserSimilarity, Long> {
    
    /**
     * İki kullanıcı arasındaki benzerliği bulur.
     * 
     * @param user1 Birinci kullanıcı
     * @param user2 İkinci kullanıcı
     * @return İki kullanıcı arasındaki benzerlik, yoksa boş Optional
     */
    Optional<UserSimilarity> findByUser1AndUser2(User user1, User user2);
    
    /**
     * Belirli bir kullanıcıya en benzer kullanıcıları bulur.
     * 
     * @param user Kullanıcı
     * @param pageable Sayfalandırma bilgisi
     * @return Benzerlik skoru en yüksek kullanıcılar
     */
    @Query("SELECT us FROM UserSimilarity us WHERE us.user1 = :user ORDER BY us.similarityScore DESC")
    Page<UserSimilarity> findMostSimilarUsers(User user, Pageable pageable);
    
    /**
     * İki kullanıcı arasındaki benzerliğin varlığını kontrol eder.
     * 
     * @param user1 Birinci kullanıcı
     * @param user2 İkinci kullanıcı
     * @return Benzerlik varsa true, yoksa false
     */
    boolean existsByUser1AndUser2(User user1, User user2);
    
    /**
     * Belirli bir kullanıcıya ait tüm benzerlikleri bulur.
     * 
     * @param user Kullanıcı
     * @return Kullanıcının tüm benzerlikleri
     */
    List<UserSimilarity> findByUser1(User user);
    
    /**
     * Belirli bir kullanıcının benzerlik skorlarını sıralar
     * 
     * @param user Kullanıcı
     * @return Benzerlik skoru en yüksekten en düşüğe sıralı liste
     */
    List<UserSimilarity> findByUser1OrderBySimilarityScoreDesc(User user);
    
    /**
     * Belirli bir kullanıcıya ait tüm benzerlikleri siler
     * 
     * @param user Kullanıcı
     */
    void deleteByUser1(User user);
}
