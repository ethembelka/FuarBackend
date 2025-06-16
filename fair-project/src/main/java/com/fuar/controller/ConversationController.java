package com.fuar.controller;

import com.fuar.dto.ConversationDTO;
import com.fuar.dto.ConversationListDTO;
import com.fuar.dto.MessageDTO;
import com.fuar.dto.SendMessageRequest;
import com.fuar.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final MessageService messageService;

    /**
     * Get all conversations for the current user
     */
    @GetMapping
    public ResponseEntity<ConversationListDTO> getUserConversations() {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(messageService.getUserConversations(currentUserId));
    }

    /**
     * Get a conversation by ID with messages
     */
    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> getConversation(@PathVariable Long conversationId) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(messageService.getConversation(conversationId, currentUserId));
    }

    /**
     * Get or create a conversation with another user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ConversationDTO> getConversationWithUser(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(messageService.getOrCreateConversation(currentUserId, userId));
    }

    /**
     * Send a message to another user
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            Long currentUserId = getCurrentUserId();
            System.out.println("REST API sending message: currentUserId=" + currentUserId + ", request=" + request);
            MessageDTO result = messageService.sendMessage(currentUserId, request.getRecipientId(), request.getContent());
            System.out.println("Message sent successfully via REST API: messageId=" + result.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error sending message via REST API: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    /**
     * Mark all messages in a conversation as read
     */
    @PostMapping("/{conversationId}/read")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable Long conversationId) {
        Long currentUserId = getCurrentUserId();
        int updatedCount = messageService.markMessagesAsRead(conversationId, currentUserId);
        return ResponseEntity.ok(updatedCount);
    }

    /**
     * Helper method to get the current authenticated user's ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        
        // The username is the email in our system
        String email = authentication.getName();
        
        // Since we need the user ID, we need to extract it from the principal
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.fuar.model.User) {
            return ((com.fuar.model.User) principal).getId();
        } else {
            // If principal is not a User object, we need to look up the user by email
            // This is handled by the service methods which will throw appropriate exceptions
            throw new IllegalStateException("Cannot determine user ID from authentication");
        }
    }
}
