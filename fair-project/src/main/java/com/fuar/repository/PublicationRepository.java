package com.fuar.repository;

import com.fuar.model.Publication;
import com.fuar.model.PublicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PublicationRepository extends JpaRepository<Publication, Long> {
    List<Publication> findByUserInfo_Id(Long userInfoId);
    
    List<Publication> findByUserInfo_IdOrderByPublicationDateDesc(Long userInfoId);
    
    List<Publication> findByPublicationType(PublicationType publicationType);
    
    @Query("SELECT p FROM Publication p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.authors) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.publisher) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Publication> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Publication p WHERE p.doi = :doi")
    List<Publication> findByDoi(@Param("doi") String doi);
}
