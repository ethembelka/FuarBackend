package com.fuar.repository;

import com.fuar.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
    
    @Query("SELECT s FROM Skill s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Skill> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT s FROM Skill s JOIN s.userInfos ui GROUP BY s ORDER BY COUNT(ui) DESC")
    List<Skill> findMostPopularSkills();
    
    boolean existsByName(String name);
    
    @Query("SELECT s FROM Skill s JOIN s.userInfos ui WHERE ui.id = :userInfoId")
    List<Skill> findByUserInfoId(@Param("userInfoId") Long userInfoId);
}
