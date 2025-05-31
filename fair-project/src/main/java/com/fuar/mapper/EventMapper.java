package com.fuar.mapper;

import com.fuar.dto.EventResponseDTO;
import com.fuar.dto.UserSummaryDTO;
import com.fuar.model.Event;
import com.fuar.model.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EventMapper {

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
}
