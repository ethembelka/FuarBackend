package com.fuar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryDTO {
    private Long id;
    private UserDTO initiator;
    private UserDTO recipient;
    private UserDTO otherUser; // The user who is not the current user
    private LocalDateTime updatedAt;
    private String lastMessageContent;
    private LocalDateTime lastMessageTimestamp;
    private int unreadCount;
}
