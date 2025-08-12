package com.fuar.recommendation.repository;

import com.fuar.model.User;
import com.fuar.recommendation.model.UserRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * UserRecommendation varlığı için veri erişim katmanı.
 */
@Repository
public interface UserRecommendationRepository extends JpaRepository<UserRecommendation, Long> {
    
    /**
     * Belirli bir kullanıcıya yapılan önerileri bulur.
     * 
     * @param targetUser Hedef kullanıcı
     * @param pageable Sayfalandırma bilgisi
     * @return Kullanıcıya yapılan öneriler
     */
    Page<UserRecommendation> findByTargetUserOrderByScoreDesc(User targetUser, Pageable pageable);
    
    /**
     * Eski arayüz uyumluluğu için targetUser -> user alias metodu
     * 
     * @param user Hedef kullanıcı
     * @return Kullanıcıya yapılan öneriler
     */
    default List<UserRecommendation> findByUserOrderByScoreDesc(User user) {
        return findByTargetUserOrderByScoreDesc(user, Pageable.unpaged()).getContent();
    }
    
    /**
     * Belirli bir kullanıcının önerilerini siler
     * 
     * @param user Hedef kullanıcı
     */
    @org.springframework.transaction.annotation.Transactional
    void deleteByTargetUser(User user);
    
    /**
     * Eski arayüz uyumluluğu için alias metodu
     * 
     * @param user Hedef kullanıcı
     */
    @org.springframework.transaction.annotation.Transactional
    default void deleteByUser(User user) {
        deleteByTargetUser(user);
    }
    
    /**
     * Belirli bir kullanıcıya yapılan, henüz görülmemiş önerileri bulur.
     * 
     * @param targetUser Hedef kullanıcı
     * @param pageable Sayfalandırma bilgisi
     * @return Kullanıcıya yapılan, henüz görülmemiş öneriler
     */
    Page<UserRecommendation> findByTargetUserAndSeenAtIsNullOrderByScoreDesc(User targetUser, Pageable pageable);
    
    /**
     * Belirli bir tarihten sonra yapılan önerileri bulur.
     * 
     * @param date Tarih
     * @param pageable Sayfalandırma bilgisi
     * @return Belirtilen tarihten sonra yapılan öneriler
     */
    Page<UserRecommendation> findByCreatedAtAfter(Date date, Pageable pageable);
    
    /**
     * Belirli bir kullanıcıya yapılan önerilerin sayısını bulur.
     * 
     * @param targetUser Hedef kullanıcı
     * @return Kullanıcıya yapılan öneri sayısı
     */
    long countByTargetUser(User targetUser);
    
    /**
     * İki kullanıcı arasındaki öneriyi bulur.
     * 
     * @param targetUser Hedef kullanıcı
     * @param recommendedUser Önerilen kullanıcı
     * @return İki kullanıcı arasındaki öneri
     */
    List<UserRecommendation> findByTargetUserAndRecommendedUser(User targetUser, User recommendedUser);
    
    /**
     * Belirli bir öneri türüne sahip önerileri bulur.
     * 
     * @param recommendationType Öneri türü
     * @param pageable Sayfalandırma bilgisi
     * @return Belirtilen öneri türüne sahip öneriler
     */
    Page<UserRecommendation> findByRecommendationType(UserRecommendation.RecommendationType recommendationType, Pageable pageable);
}
