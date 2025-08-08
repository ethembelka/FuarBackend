package com.fuar.controller;

import com.fuar.dto.EventSessionDTO;
import com.fuar.service.EventSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/event-sessions")
@RequiredArgsConstructor
public class EventSessionController {
    
    private final EventSessionService eventSessionService;
    
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventSessionDTO>> getSessionsByEventId(@PathVariable Long eventId) {
        List<EventSessionDTO> sessions = eventSessionService.getSessionsByEventId(eventId);
        return ResponseEntity.ok(sessions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EventSessionDTO> getSessionById(@PathVariable Long id) {
        EventSessionDTO session = eventSessionService.getEventSessionById(id);
        return ResponseEntity.ok(session);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventSessionDTO> createEventSession(@Valid @RequestBody EventSessionDTO sessionDTO) {
        EventSessionDTO createdSession = eventSessionService.createEventSession(sessionDTO);
        return new ResponseEntity<>(createdSession, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventSessionDTO> updateEventSession(
            @PathVariable Long id,
            @Valid @RequestBody EventSessionDTO sessionDTO) {
        EventSessionDTO updatedSession = eventSessionService.updateEventSession(id, sessionDTO);
        return ResponseEntity.ok(updatedSession);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEventSession(@PathVariable Long id) {
        eventSessionService.deleteEventSession(id);
        return ResponseEntity.noContent().build();
    }
}
