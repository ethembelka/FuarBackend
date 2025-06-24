package com.fuar.controller;

import com.fuar.dto.EventDTO;
import com.fuar.dto.EventResponseDTO;
import com.fuar.dto.EventUpdateDTO;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("/image/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadEventImage(
            @RequestParam("image") MultipartFile file
    ) {
        try {
            // Validate the file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            // Limit file size (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File size exceeds maximum limit of 5MB");
            }

            // Get project root directory
            String projectRoot = System.getProperty("user.dir");

            // Generate a unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = "event_" + System.currentTimeMillis() + extension;

            // Set up possible upload directories
            String[] uploadDirs = {
                projectRoot + "/uploads/eventPhotos/",
                System.getProperty("java.io.tmpdir") + "/uploads/eventPhotos/",
                System.getProperty("user.home") + "/uploads/eventPhotos/",
                "uploads/eventPhotos/"
            };

            File savedFile = null;
            String usedDirectory = null;
            Exception lastException = null;

            // Try each directory until successful
            for (String dir : uploadDirs) {
                File directory = new File(dir);

                // Create directory if it doesn't exist
                if (!directory.exists()) {
                    try {
                        boolean created = directory.mkdirs();
                        if (created) {
                            // Try to make directory world-readable on Unix systems
                            try {
                                Process process = Runtime.getRuntime().exec("chmod 755 " + directory.getAbsolutePath());
                                int exitCode = process.waitFor();
                                if (exitCode == 0) {
                                    System.out.println("Set permissions for directory: " + directory.getAbsolutePath());
                                }
                            } catch (Exception e) {
                                System.out.println("Could not set permissions: " + e.getMessage());
                            }
                        } else {
                            System.err.println("Failed to create directory: " + directory.getAbsolutePath());
                            continue;
                        }
                    } catch (Exception e) {
                        System.err.println("Error creating directory " + directory.getAbsolutePath() + ": " + e.getMessage());
                        continue;
                    }
                }

                if (!directory.canWrite()) {
                    System.err.println("Directory not writable: " + directory.getAbsolutePath());
                    continue;
                }

                File targetFile = new File(directory, filename);

                try {
                    file.transferTo(targetFile);

                    if (targetFile.exists() && targetFile.length() > 0) {
                        System.out.println("File saved successfully to: " + targetFile.getAbsolutePath());
                        savedFile = targetFile;
                        usedDirectory = dir;
                        break;
                    } else {
                        System.err.println("File saving verification failed: " + targetFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    System.err.println("Error saving to " + targetFile.getAbsolutePath() + ": " + e.getMessage());
                    lastException = e;
                }
            }

            // If all attempts failed, return an error
            if (savedFile == null) {
                String errorMsg = "Failed to save file to any location";
                if (lastException != null) {
                    errorMsg += ": " + lastException.getMessage();
                }
                System.err.println(errorMsg);
                return ResponseEntity.status(500).body(errorMsg);
            }

            // Create the URL path for the image
            String imagePath = "/uploads/eventPhotos/" + filename;

            System.out.println("Event image upload successful:");
            System.out.println("- File saved to: " + savedFile.getAbsolutePath());
            System.out.println("- Image URL path: " + imagePath);

            // Return the image URL in the response
            Map<String, String> response = new HashMap<>();
            response.put("url", imagePath);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error uploading event image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error uploading event image: " + e.getMessage());
        }
    }

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
        
        // Process image URL to store only the relative path
        String imageUrl = eventDTO.getImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // If it's a full URL, extract only the path part starting from /uploads
            if (imageUrl.contains("/uploads/")) {
                int uploadsIndex = imageUrl.indexOf("/uploads/");
                imageUrl = imageUrl.substring(uploadsIndex);
            }
        }
        event.setImage(imageUrl);

        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.ok(eventMapper.toResponseDTO(createdEvent));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable @NotNull Long id,
            @RequestBody @Valid EventUpdateDTO eventUpdateDTO
    ) {
        // Ensure the ID in the path matches the ID in the DTO
        if (!id.equals(eventUpdateDTO.getId())) {
            throw new IllegalArgumentException("ID in path must match ID in request body");
        }
        
        Event event = new Event();
        event.setTitle(eventUpdateDTO.getTitle());
        event.setDescription(eventUpdateDTO.getDescription());
        event.setLocation(eventUpdateDTO.getLocation());
        event.setStartDate(eventUpdateDTO.getStartDate());
        event.setEndDate(eventUpdateDTO.getEndDate());
        event.setCapacity(eventUpdateDTO.getCapacity());
        
        // Process image URL to store only the relative path
        String imageUrl = eventUpdateDTO.getImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // If it's a full URL, extract only the path part starting from /uploads
            if (imageUrl.contains("/uploads/")) {
                int uploadsIndex = imageUrl.indexOf("/uploads/");
                imageUrl = imageUrl.substring(uploadsIndex);
            }
        }
        event.setImage(imageUrl);

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

    @GetMapping("/{eventId}/attendees/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkUserAttendance(
            @PathVariable Long eventId,
            @RequestParam Long userId
    ) {
        boolean isAttending = eventService.isUserAttending(eventId, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAttending", isAttending);
        return ResponseEntity.ok(response);
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
