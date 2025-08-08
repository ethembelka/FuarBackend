package com.fuar.mapper;

import com.fuar.dto.EventDTO;
import com.fuar.dto.EventResponseDTO;
import com.fuar.dto.EventSessionDTO;
import com.fuar.dto.UserSummaryDTO;
import com.fuar.model.Event;
import com.fuar.model.EventSession;
import com.fuar.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapper {
    
    private final EventSessionMapper sessionMapper;

    public EventResponseDTO toResponseDTO(Event event) {
        if (event == null) {
            return null;
        }

        try {
            System.out.println("Event mapping başlıyor: " + event.getId());
            
            EventResponseDTO dto = EventResponseDTO.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .location(event.getLocation())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .capacity(event.getCapacity())
                    .image(event.getImage())
                    .createdAt(event.getCreatedAt())
                    .updatedAt(event.getUpdatedAt())
                    .build();
                    
            // Event oturumlarını dönüştür ve başlangıç zamanına göre sırala
            if (event.getSessions() != null && !event.getSessions().isEmpty()) {
                List<EventSessionDTO> sessionDTOs = event.getSessions().stream()
                    .sorted((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime())) // Başlangıç zamanına göre artan sıralama
                    .map(sessionMapper::toDto)
                    .collect(Collectors.toList());
                dto.setSessions(sessionDTOs);
            }
            
            System.out.println("Temel event özellikleri dönüştürüldü, speakers'a geçiliyor");
            
            // Speakers'ları ayrı bir try-catch bloğunda işleyelim
            try {
                if (event.getSpeakers() != null) {
                    System.out.println("Event " + event.getId() + " için " + event.getSpeakers().size() + " speaker bulundu");
                    java.util.Set<UserSummaryDTO> speakerDTOs = new java.util.HashSet<>();
                    
                    for (User speaker : event.getSpeakers()) {
                        try {
                            if (speaker != null) {
                                System.out.println("Speaker ID: " + speaker.getId() + " dönüştürülüyor");
                                UserSummaryDTO speakerDTO = UserSummaryDTO.builder()
                                    .id(speaker.getId())
                                    .name(speaker.getName())
                                    .email(speaker.getEmail())
                                    .image(speaker.getImage())
                                    .build();
                                speakerDTOs.add(speakerDTO);
                                System.out.println("Speaker ID: " + speaker.getId() + " başarıyla dönüştürüldü");
                            }
                        } catch (Exception e) {
                            System.err.println("Speaker dönüştürülürken hata: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    dto.setSpeakers(speakerDTOs);
                    System.out.println("Tüm speakerlar başarıyla dönüştürüldü");
                } else {
                    System.out.println("Event " + event.getId() + " için speaker listesi null");
                    dto.setSpeakers(new java.util.HashSet<>());
                }
            } catch (Exception e) {
                System.err.println("Speakers listesi dönüştürülürken hata: " + e.getMessage());
                e.printStackTrace();
                dto.setSpeakers(new java.util.HashSet<>());
            }
            
            System.out.println("Attendees dönüşümüne geçiliyor");
            
            // Attendees için aynı mantıkla dönüşüm yapalım
            try {
                if (event.getAttendees() != null) {
                    System.out.println("Event " + event.getId() + " için " + event.getAttendees().size() + " attendee bulundu");
                    java.util.Set<UserSummaryDTO> attendeeDTOs = new java.util.HashSet<>();
                    
                    for (User attendee : event.getAttendees()) {
                        try {
                            if (attendee != null) {
                                System.out.println("Attendee ID: " + attendee.getId() + " dönüştürülüyor");
                                UserSummaryDTO attendeeDTO = UserSummaryDTO.builder()
                                    .id(attendee.getId())
                                    .name(attendee.getName())
                                    .email(attendee.getEmail())
                                    .image(attendee.getImage())
                                    .build();
                                attendeeDTOs.add(attendeeDTO);
                                System.out.println("Attendee ID: " + attendee.getId() + " başarıyla dönüştürüldü");
                            }
                        } catch (Exception e) {
                            System.err.println("Attendee dönüştürülürken hata: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    dto.setAttendees(attendeeDTOs);
                    System.out.println("Tüm attendeeler başarıyla dönüştürüldü");
                } else {
                    System.out.println("Event " + event.getId() + " için attendee listesi null");
                    dto.setAttendees(new java.util.HashSet<>());
                }
            } catch (Exception e) {
                System.err.println("Attendees listesi dönüştürülürken hata: " + e.getMessage());
                e.printStackTrace();
                dto.setAttendees(new java.util.HashSet<>());
            }
            
            System.out.println("Event ID: " + event.getId() + " dönüşümü tamamlandı");
            return dto;
            
        } catch (Exception e) {
            System.err.println("Event dönüştürülürken genel hata: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Event dönüştürülürken hata: " + e.getMessage(), e);
        }
    }
    
    private UserSummaryDTO toUserSummaryDTO(User user) {
        if (user == null) {
            return null;
        }
        
        try {
            Long userId = user.getId();
            String userName = null;
            String userEmail = null;
            String userImage = null;
            String userBio = null;
            String userHeadLine = null;
            String userLocation = null;
            
            // Use try-catch for each property to prevent any single property 
            // from causing the entire mapping to fail
            try { userName = user.getName(); } catch (Exception e) { 
                System.err.println("Error getting name for user ID: " + userId); 
            }
            
            try { userEmail = user.getEmail(); } catch (Exception e) { 
                System.err.println("Error getting email for user ID: " + userId); 
            }
            
            try { userImage = user.getImage(); } catch (Exception e) { 
                System.err.println("Error getting image for user ID: " + userId); 
            }
            
            // Safely get UserInfo properties
            if (user.getUserInfo() != null) {
                try { userBio = user.getUserInfo().getBio(); } catch (Exception e) {
                    System.err.println("Error getting bio for user ID: " + userId);
                }
                
                try { userHeadLine = user.getUserInfo().getHeadLine(); } catch (Exception e) {
                    System.err.println("Error getting headline for user ID: " + userId);
                }
                
                try { userLocation = user.getUserInfo().getLocation(); } catch (Exception e) {
                    System.err.println("Error getting location for user ID: " + userId);
                }
            }
            
            return UserSummaryDTO.builder()
                .id(userId)
                .name(userName)
                .email(userEmail)
                .image(userImage)
                .bio(userBio)
                .headLine(userHeadLine)
                .location(userLocation)
                .build();
        } catch (Exception e) {
            System.err.println("Critical error mapping user: " + e.getMessage());
            e.printStackTrace();
            return null; // Return null to be filtered out in the stream
        }
    }
    
    public Event toEntity(EventDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Event event = Event.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .capacity(dto.getCapacity())
                .image(dto.getImage())
                .speakers(new HashSet<>())
                .attendees(new HashSet<>())
                .sessions(new ArrayList<>())
                .build();
                
        // Session'ları işle
        if (dto.getSessions() != null && !dto.getSessions().isEmpty()) {
            List<EventSession> sessions = new ArrayList<>();
            for (EventSessionDTO sessionDTO : dto.getSessions()) {
                EventSession session = sessionMapper.toEntity(sessionDTO);
                session.setEvent(event);
                sessions.add(session);
            }
            event.setSessions(sessions);
        }
        
        return event;
    }
}
