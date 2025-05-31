package com.fuar.repository;

import com.fuar.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStartDateAfterOrderByStartDate(LocalDateTime date);
    
    @Query("SELECT e FROM Event e WHERE e.startDate > :now AND e.capacity > (SELECT COUNT(a) FROM e.attendees a)")
    List<Event> findAvailableEvents(@Param("now") LocalDateTime now);
    
    List<Event> findBySpeakersId(Long speakerId);
    
    @Query("SELECT e FROM Event e WHERE :userId IN (SELECT a.id FROM e.attendees a)")
    List<Event> findEventsByAttendeeId(@Param("userId") Long userId);
    
    @Query("SELECT e FROM Event e WHERE " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :startDate AND :endDate")
    List<Event> findEventsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
