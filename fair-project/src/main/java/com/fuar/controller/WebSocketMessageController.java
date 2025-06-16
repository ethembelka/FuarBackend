/**
 * WebSocket Message Controller
 * 
 * Bu controller, gerçek zamanlı mesajlaşma için WebSocket bağlantılarını yönetir.
 * STOMP protokolü kullanılarak mesajların iletilmesini, okunma durumlarının güncellenmesini
 * ve yazma göstergelerinin (typing indicators) gönderilmesini sağlar.
 * 
 * Temel işlevler:
 * - Özel mesajların gönderilmesi ve alınması
 * - Mesajların okundu olarak işaretlenmesi
 * - Yazma göstergelerinin yönetimi
 */
package com.fuar.controller;

import com.fuar.dto.MessageDTO;
import com.fuar.dto.SendMessageRequest;
import com.fuar.dto.TypingIndicatorRequest;
import com.fuar.model.User;
import com.fuar.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket mesaj iletişimini yöneten controller sınıfı.
 * Bu sınıf, kullanıcılar arasındaki gerçek zamanlı mesajlaşmayı yönetir.
 */
@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * WebSocket üzerinden gönderilen özel mesajları işler.
     * İstemciler şu endpoint'e gönderir: /app/chat.send
     * 
     * @param messageRequest Mesaj isteği (içerik, alıcı ID vs.)
     * @param principal Kimlik doğrulama bilgilerini içeren Principal nesnesi
     * @param headerAccessor Header bilgilerine erişim sağlayan nesne
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest messageRequest, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        // Extract the sender's information from the principal
        if (principal == null) {
            System.err.println("WebSocket authentication error: User not authenticated");
            throw new IllegalStateException("User not authenticated");
        }
        
        try {
            // Get sender's ID
            Long senderId = extractUserId(principal);
            
            // Log the request and headers
            System.out.println("-------------------------------------");
            System.out.println("WebSocket message received via STOMP:");
            System.out.println("From user: " + principal.getName());
            System.out.println("Sender ID: " + senderId);
            System.out.println("Recipient ID: " + messageRequest.getRecipientId());
            System.out.println("Content: " + messageRequest.getContent());
            if (headerAccessor != null) {
                System.out.println("Session ID: " + headerAccessor.getSessionId());
                System.out.println("Message headers: " + headerAccessor.getMessageHeaders());
            }
            
            // Add some additional debugging info for WebSocket session
            if (headerAccessor != null && headerAccessor.getSessionAttributes() != null) {
                System.out.println("Session attributes: " + headerAccessor.getSessionAttributes());
            }
            
            // Send the message and let the service handle the WebSocket notification
            MessageDTO messageDTO = messageService.sendMessage(senderId, messageRequest.getRecipientId(), messageRequest.getContent());
            
            // Directly send message to recipient via WebSocket controller too
            // This provides a redundant delivery path in case the service method fails
            messagingTemplate.convertAndSendToUser(
                    messageRequest.getRecipientId().toString(),
                    "/queue/messages",
                    messageDTO
            );
            
            // Log successful message processing
            System.out.println("Message processed successfully via WebSocket.");
            System.out.println("Created message with ID: " + messageDTO.getId());
            System.out.println("Message will be delivered to user: " + messageRequest.getRecipientId());
            System.out.println("-------------------------------------");
        } catch (Exception e) {
            System.err.println("Error processing WebSocket message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Mark messages as read via WebSocket
     * Clients will send to: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload Long conversationId, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        Long userId = extractUserId(principal);
        messageService.markMessagesAsRead(conversationId, userId);
    }
    
    /**
     * Handle typing indicator
     * Clients will send to: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTypingIndicator(@Payload TypingIndicatorRequest request, Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        try {
            Long userId = extractUserId(principal);
            Long otherUserId = messageService.getOtherUserInConversation(request.getConversationId(), userId);
            
            Map<String, Object> typingData = new HashMap<>();
            typingData.put("conversationId", request.getConversationId());
            typingData.put("userId", userId);
            typingData.put("isTyping", request.isTyping());
            
            messagingTemplate.convertAndSendToUser(
                    otherUserId.toString(),
                    "/queue/typing",
                    typingData
            );
        } catch (Exception e) {
            System.err.println("Error processing typing indicator: " + e.getMessage());
        }
    }
    
    /**
     * Debug endpoint for testing WebSocket connectivity
     * Clients will send to: /app/debug.test
     */
    @MessageMapping("/debug.test")
    public void handleDebugMessage(@Payload Map<String, Object> debugMessage, Principal principal) {
        if (principal == null) {
            System.err.println("Debug test: User not authenticated");
            return;
        }
        
        try {
            Long userId = extractUserId(principal);
            String testId = (String) debugMessage.get("testId");
            
            System.out.println("-------------------------------------");
            System.out.println("Debug test message received:");
            System.out.println("From user: " + principal.getName());
            System.out.println("User ID: " + userId);
            System.out.println("Test ID: " + testId);
            
            // Send echo response back to the same user
            Map<String, Object> response = new HashMap<>(debugMessage);
            response.put("received", true);
            response.put("responseTime", System.currentTimeMillis());
            
            // Send through all possible channels for testing
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/debug",
                    response
            );
            
            // Also try other delivery methods
            messagingTemplate.convertAndSend(
                    "/queue/debug/" + userId,
                    response
            );
            
            messagingTemplate.convertAndSend(
                    "/topic/debug/" + userId,
                    response
            );
            
            System.out.println("Debug test response sent back to user: " + userId);
            System.out.println("-------------------------------------");
        } catch (Exception e) {
            System.err.println("Error processing debug message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Debug endpoint to track message delivery status
     * Clients will send to: /app/chat.debug
     */
    @MessageMapping("/chat.debug")
    public void debugMessageDelivery(@Payload Map<String, Object> debugInfo, Principal principal) {
        if (principal == null) {
            System.err.println("Debug: User not authenticated");
            return;
        }
        
        try {
            Long userId = extractUserId(principal);
            System.out.println("-------------------------------------");
            System.out.println("DEBUG MESSAGE DELIVERY STATUS:");
            System.out.println("From User ID: " + userId);
            System.out.println("Debug Info: " + debugInfo);
            
            // Echo back the debug info to the sender
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/debug",
                    Map.of(
                        "status", "received",
                        "timestamp", System.currentTimeMillis(),
                        "originalData", debugInfo
                    )
            );
            
            // Also try sending via multiple channels for testing
            messagingTemplate.convertAndSend(
                    "/queue/debug/" + userId,
                    Map.of(
                        "status", "received_queue",
                        "timestamp", System.currentTimeMillis()
                    )
            );
            
            messagingTemplate.convertAndSend(
                    "/topic/debug/" + userId,
                    Map.of(
                        "status", "received_topic",
                        "timestamp", System.currentTimeMillis()
                    )
            );
            
            System.out.println("Debug response sent via multiple channels");
            System.out.println("-------------------------------------");
        } catch (Exception e) {
            System.err.println("Error in debug endpoint: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Extract user ID from Principal
     */
    private Long extractUserId(Principal principal) {
        try {
            if (principal instanceof Authentication) {
                Authentication auth = (Authentication) principal;
                Object userObj = auth.getPrincipal();
                
                if (userObj instanceof User) {
                    return ((User) userObj).getId();
                } else {
                    // Try to parse the name as a user ID
                    String name = principal.getName();
                    try {
                        return Long.parseLong(name);
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to parse user ID from principal name: " + name);
                        // Continue to the fallback below
                    }
                }
            }
            
            // Fallback: try to find user by principal name
            // This assumes that the WebSocketAuthInterceptor has set the principal name to be the user ID
            try {
                return Long.parseLong(principal.getName());
            } catch (NumberFormatException e) {
                System.err.println("Cannot parse user ID from principal name: " + principal.getName());
                System.err.println("This may indicate a problem with authentication in WebSocketAuthInterceptor");
                throw new IllegalStateException("Unable to determine user ID from WebSocket connection");
            }
        } catch (Exception e) {
            System.err.println("Error extracting user ID from principal: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Failed to extract user ID", e);
        }
    }
}
