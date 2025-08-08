package com.fuar.mapper;

import com.fuar.dto.EventSessionDTO;
import com.fuar.model.Event;
import com.fuar.model.EventSession;
import com.fuar.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventSessionMapper {

    private final EventService eventService;

    public EventSession toEntity(EventSessionDTO dto) {
        if (dto == null) {
            return null;
        }

        EventSession entity = new EventSession();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setSpeakerName(dto.getSpeakerName());
        entity.setLocation(dto.getLocation());
        
        if (dto.getEventId() != null) {
            Event event = eventService.getEventById(dto.getEventId());
            entity.setEvent(event);
        }
        
        return entity;
    }

    public EventSessionDTO toDto(EventSession entity) {
        if (entity == null) {
            return null;
        }

        return EventSessionDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .speakerName(entity.getSpeakerName())
                .location(entity.getLocation())
                .eventId(entity.getEvent() != null ? entity.getEvent().getId() : null)
                .build();
    }
}
