package com.fuar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private UserDTO initiator;
    private UserDTO recipient;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private MessageDTO lastMessage;
    private int unreadCount;
    private List<MessageDTO> messages;
}
