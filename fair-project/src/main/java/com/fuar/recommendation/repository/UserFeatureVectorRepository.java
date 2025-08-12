package com.fuar.recommendation.repository;

import com.fuar.model.User;
import com.fuar.recommendation.model.UserFeatureVector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserFeatureVector varlığı için veri erişim katmanı.
 */
@Repository
public interface UserFeatureVectorRepository extends JpaRepository<UserFeatureVector, Long> {
    
    /**
     * Belirli bir kullanıcı için özellik vektörünü bulur.
     * 
     * @param user Kullanıcı
     * @return Kullanıcının özellik vektörü, yoksa boş Optional
     */
    Optional<UserFeatureVector> findByUser(User user);
}
