package com.fuar.repository;

import com.fuar.model.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {
    List<WorkExperience> findByUserInfo_Id(Long userInfoId);
    
    List<WorkExperience> findByUserInfo_IdOrderByStartDateDesc(Long userInfoId);
    
    @Query("SELECT w FROM WorkExperience w WHERE " +
           "LOWER(w.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.position) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<WorkExperience> searchByKeyword(@Param("keyword") String keyword);
    
    List<WorkExperience> findByCompanyAndCurrent(String company, boolean current);
}
