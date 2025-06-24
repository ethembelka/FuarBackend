package com.fuar.service;

import com.fuar.exception.BadRequestException;
import com.fuar.exception.ResourceNotFoundException;
import com.fuar.model.Event;
import com.fuar.model.User;
import com.fuar.repository.EventRepository;
import com.fuar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        System.out.println("EventService.getAllEvents çağrıldı");
        List<Event> events = eventRepository.findAll();
        System.out.println("Toplam " + events.size() + " event bulundu");
        
        // Lazy loading sorunlarını önlemek için koleksiyonları önceden yükleyelim
        for (Event event : events) {
            if (event.getSpeakers() != null) {
                System.out.println("Event ID: " + event.getId() + " için " + event.getSpeakers().size() + " speaker önceden yükleniyor");
            }
            if (event.getAttendees() != null) {
                System.out.println("Event ID: " + event.getId() + " için " + event.getAttendees().size() + " attendee önceden yükleniyor");
            }
        }
        
        return events;
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    public List<Event> getUpcomingEvents() {
        return eventRepository.findByStartDateAfterOrderByStartDate(LocalDateTime.now());
    }

    public List<Event> getAvailableEvents() {
        return eventRepository.findAvailableEvents(LocalDateTime.now());
    }

    @Transactional
    public Event createEvent(Event event) {
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEvent(Long id, Event eventDetails) {
        Event event = getEventById(id);

        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setLocation(eventDetails.getLocation());
        event.setStartDate(eventDetails.getStartDate());
        event.setEndDate(eventDetails.getEndDate());
        event.setCapacity(eventDetails.getCapacity());
        event.setImage(eventDetails.getImage());
        event.setUpdatedAt(LocalDateTime.now());

        return eventRepository.save(event);
    }

    @Transactional
    public Event addSpeaker(Long eventId, Long userId) {
        Event event = getEventById(eventId);
        User speaker = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        event.getSpeakers().add(speaker);
        return eventRepository.save(event);
    }

    @Transactional
    public Event removeSpeaker(Long eventId, Long userId) {
        Event event = getEventById(eventId);
        User speaker = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        event.getSpeakers().remove(speaker);
        return eventRepository.save(event);
    }

    @Transactional
    public Event registerAttendee(Long eventId, Long userId) {
        Event event = getEventById(eventId);
        User attendee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (event.getCapacity() != null && 
            event.getAttendees().size() >= event.getCapacity()) {
            throw new BadRequestException("Event is at full capacity");
        }

        event.getAttendees().add(attendee);
        return eventRepository.save(event);
    }

    @Transactional
    public Event unregisterAttendee(Long eventId, Long userId) {
        Event event = getEventById(eventId);
        User attendee = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        event.getAttendees().remove(attendee);
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    public List<Event> searchEvents(String keyword) {
        return eventRepository.searchByKeyword(keyword);
    }

    public List<Event> getEventsByAttendee(Long userId) {
        return eventRepository.findEventsByAttendeeId(userId);
    }

    public List<Event> getEventsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return eventRepository.findEventsBetweenDates(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsBySpeakerId(Long speakerId) {
        // Önce kullanıcının varlığını kontrol et
        User speaker = userRepository.findById(speakerId)
                .orElseThrow(() -> new ResourceNotFoundException("Konuşmacı bulunamadı: " + speakerId));

        // JPA repository'de yeni bir metot kullanarak etkinlikleri getir
        List<Event> events = eventRepository.findBySpeakersId(speakerId);
        
        // Lazy loading sorunlarını önlemek için koleksiyonları önceden yükle
        for (Event event : events) {
            if (event.getSpeakers() != null) {
                event.getSpeakers().size(); // Initialize the collection
            }
            if (event.getAttendees() != null) {
                event.getAttendees().size(); // Initialize the collection
            }
        }
        
        return events;
    }

    /**
     * Remove a user from all events where they are a speaker
     * @param userId User ID to remove
     */
    @Transactional
    public void removeUserFromAllEvents(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get all events where user is a speaker
        List<Event> events = eventRepository.findBySpeakersId(userId);
        
        // Remove user from speakers list in each event
        for (Event event : events) {
            event.getSpeakers().remove(user);
            eventRepository.save(event);
        }
    }

    /**
     * Remove a user from all events where they are an attendee
     * @param userId User ID to remove
     */
    @Transactional
    public void unregisterFromAllEvents(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get all events where user is an attendee
        List<Event> events = eventRepository.findEventsByAttendeeId(userId);
        
        // Remove user from attendees list in each event
        for (Event event : events) {
            event.getAttendees().remove(user);
            eventRepository.save(event);
        }
    }

    /**
     * Check if a user is attending a specific event
     * @param eventId Event ID
     * @param userId User ID
     * @return true if user is attending, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isUserAttending(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        return event.getAttendees().contains(user);
    }
}
