package com.fuar.repository;

import com.fuar.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByUserInfo_Id(Long userInfoId);
    
    List<Education> findByUserInfo_IdOrderByStartDateDesc(Long userInfoId);
    
    List<Education> findByInstitutionAndFieldOfStudy(String institution, String fieldOfStudy);
    
    @Query("SELECT e FROM Education e WHERE " +
           "LOWER(e.institution) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.fieldOfStudy) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Education> searchByKeyword(@Param("keyword") String keyword);
    
    List<Education> findByFieldOfStudyAndCurrent(String fieldOfStudy, boolean current);
}
