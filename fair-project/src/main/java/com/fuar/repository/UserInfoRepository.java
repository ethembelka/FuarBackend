package com.fuar.repository;

import com.fuar.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByUser_Id(Long userId);
    
    @Query("SELECT ui FROM UserInfo ui JOIN ui.skills s WHERE s.name = :skillName")
    List<UserInfo> findBySkillName(@Param("skillName") String skillName);
    
    @Query("SELECT ui FROM UserInfo ui WHERE " +
           "LOWER(ui.headLine) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ui.bio) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UserInfo> searchByKeyword(@Param("keyword") String keyword);
}
