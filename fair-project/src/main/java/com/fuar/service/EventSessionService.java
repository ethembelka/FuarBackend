package com.fuar.service;

import com.fuar.dto.EventSessionDTO;
import com.fuar.mapper.EventSessionMapper;
import com.fuar.model.Event;
import com.fuar.model.EventSession;
import com.fuar.repository.EventRepository;
import com.fuar.repository.EventSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventSessionService {
    
    private final EventSessionRepository eventSessionRepository;
    private final EventRepository eventRepository;
    private final EventSessionMapper eventSessionMapper;
    
    @Transactional(readOnly = true)
    public List<EventSessionDTO> getSessionsByEventId(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));
            
        return event.getSessions().stream()
            .sorted((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime())) // Oturumları başlangıç zamanına göre sırala
            .map(eventSessionMapper::toDto)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public EventSessionDTO createEventSession(EventSessionDTO sessionDTO) {
        EventSession session = eventSessionMapper.toEntity(sessionDTO);
        EventSession savedSession = eventSessionRepository.save(session);
        return eventSessionMapper.toDto(savedSession);
    }
    
    @Transactional
    public EventSessionDTO updateEventSession(Long id, EventSessionDTO sessionDTO) {
        EventSession existingSession = eventSessionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Event session not found with id: " + id));
            
        existingSession.setTitle(sessionDTO.getTitle());
        existingSession.setDescription(sessionDTO.getDescription());
        existingSession.setStartTime(sessionDTO.getStartTime());
        existingSession.setEndTime(sessionDTO.getEndTime());
        existingSession.setSpeakerName(sessionDTO.getSpeakerName());
        existingSession.setLocation(sessionDTO.getLocation());
        
        EventSession updatedSession = eventSessionRepository.save(existingSession);
        return eventSessionMapper.toDto(updatedSession);
    }
    
    @Transactional
    public void deleteEventSession(Long id) {
        eventSessionRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public EventSessionDTO getEventSessionById(Long id) {
        EventSession session = eventSessionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Event session not found with id: " + id));
        return eventSessionMapper.toDto(session);
    }
}
