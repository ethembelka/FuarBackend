package com.fuar.controller;

import com.fuar.dto.EventDTO;
import com.fuar.dto.EventResponseDTO;
import com.fuar.mapper.EventMapper;
import com.fuar.model.Event;
import com.fuar.service.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Validated
public class EventController {
    private final EventService eventService;
    private final EventMapper eventMapper;

    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        try {
            System.out.println("GET /api/v1/events çağrıldı");
            List<Event> events = eventService.getAllEvents();
            System.out.println("Events listesi alındı, boyut: " + events.size());
            
            List<EventResponseDTO> eventDTOs = new java.util.ArrayList<>();
            
            for (Event event : events) {
                try {
                    System.out.println("Event ID: " + event.getId() + " dönüştürülüyor");
                    if (event.getSpeakers() != null) {
                        System.out.println("  - Speakers sayısı: " + event.getSpeakers().size());
                    }
                    EventResponseDTO dto = eventMapper.toResponseDTO(event);
                    eventDTOs.add(dto);
                    System.out.println("Event ID: " + event.getId() + " başarıyla dönüştürüldü");
                } catch (Exception e) {
                    System.err.println("Event ID: " + event.getId() + " dönüştürülürken hata oluştu: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return ResponseEntity.ok(eventDTOs);
        } catch (Exception e) {
            System.err.println("getAllEvents metodunda hata: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of(
                    "status", 500,
                    "message", "Event verilerini alırken bir hata oluştu: " + e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
                ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable @NotNull Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(eventMapper.toResponseDTO(event));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponseDTO>> getUpcomingEvents() {
        List<Event> events = eventService.getUpcomingEvents();
        List<EventResponseDTO> eventDTOs = events.stream()
                .map(eventMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventDTOs);
    }

    @GetMapping("/available")
    public ResponseEntity<List<EventResponseDTO>> getAvailableEvents() {
        List<Event> events = eventService.getAvailableEvents();
        List<EventResponseDTO> eventDTOs = events.stream()
                .map(eventMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventDTOs);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDTO> createEvent(@RequestBody @Valid EventDTO eventDTO) {
        Event event = new Event();
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setLocation(eventDTO.getLocation());
        event.setStartDate(eventDTO.getStartDate());
        event.setEndDate(eventDTO.getEndDate());
        event.setCapacity(eventDTO.getCapacity());
        event.setImage(eventDTO.getImage());

        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.ok(eventMapper.toResponseDTO(createdEvent));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable @NotNull Long id,
            @RequestBody @Valid EventDTO eventDTO
    ) {
        Event event = new Event();
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setLocation(eventDTO.getLocation());
        event.setStartDate(eventDTO.getStartDate());
        event.setEndDate(eventDTO.getEndDate());
        event.setCapacity(eventDTO.getCapacity());
        event.setImage(eventDTO.getImage());

        Event updatedEvent = eventService.updateEvent(id, event);
        return ResponseEntity.ok(eventMapper.toResponseDTO(updatedEvent));
    }

    @PostMapping("/{eventId}/speakers/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDTO> addSpeaker(
            @PathVariable Long eventId,
            @PathVariable Long userId
    ) {
        Event event = eventService.addSpeaker(eventId, userId);
        return ResponseEntity.ok(eventMapper.toResponseDTO(event));
    }

    @DeleteMapping("/{eventId}/speakers/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDTO> removeSpeaker(
            @PathVariable Long eventId,
            @PathVariable Long userId
    ) {
        Event event = eventService.removeSpeaker(eventId, userId);
        return ResponseEntity.ok(eventMapper.toResponseDTO(event));
    }

    @PostMapping("/{eventId}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDTO> registerForEvent(
            @PathVariable Long eventId,
            @RequestParam Long userId
    ) {
        Event event = eventService.registerAttendee(eventId, userId);
        return ResponseEntity.ok(eventMapper.toResponseDTO(event));
    }

    @PostMapping("/{eventId}/unregister")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDTO> unregisterFromEvent(
            @PathVariable Long eventId,
            @RequestParam Long userId
    ) {
        Event event = eventService.unregisterAttendee(eventId, userId);
        return ResponseEntity.ok(eventMapper.toResponseDTO(event));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventResponseDTO>> searchEvents(@RequestParam String keyword) {
        List<Event> events = eventService.searchEvents(keyword);
        List<EventResponseDTO> eventDTOs = events.stream()
                .map(eventMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventDTOs);
    }

    @GetMapping("/attendee/{userId}")
    public ResponseEntity<List<EventResponseDTO>> getEventsByAttendee(@PathVariable Long userId) {
        List<Event> events = eventService.getEventsByAttendee(userId);
        List<EventResponseDTO> eventDTOs = events.stream()
                .map(eventMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventDTOs);
    }

    @GetMapping("/between")
    public ResponseEntity<List<EventResponseDTO>> getEventsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<Event> events = eventService.getEventsBetweenDates(startDate, endDate);
        List<EventResponseDTO> eventDTOs = events.stream()
                .map(eventMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventDTOs);
    }

    @GetMapping("/speaker/{speakerId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getEventsBySpeakerId(@PathVariable @NotNull Long speakerId) {
        try {
            System.out.println("GET /api/v1/events/speaker/" + speakerId + " çağrıldı");
            List<Event> events = eventService.getEventsBySpeakerId(speakerId);
            System.out.println("Konuşmacı etkinlikleri alındı, boyut: " + events.size());
            
            List<EventResponseDTO> eventDTOs = events.stream()
                .map(event -> {
                    try {
                        return eventMapper.toResponseDTO(event);
                    } catch (Exception e) {
                        System.err.println("Event ID: " + event.getId() + " dönüştürülürken hata oluştu: " + e.getMessage());
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(eventDTOs);
        } catch (Exception e) {
            System.err.println("Konuşmacı etkinlikleri alınırken hata: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Konuşmacı etkinlikleri alınırken bir hata oluştu");
        }
    }
}
