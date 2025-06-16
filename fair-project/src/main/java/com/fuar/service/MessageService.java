package com.fuar.service;

import com.fuar.dto.ConversationDTO;
import com.fuar.dto.ConversationListDTO;
import com.fuar.dto.ConversationSummaryDTO;
import com.fuar.dto.MessageDTO;
import com.fuar.exception.ResourceNotFoundException;
import com.fuar.mapper.MessageMapper;
import com.fuar.model.Conversation;
import com.fuar.model.Message;
import com.fuar.model.User;
import com.fuar.repository.ConversationRepository;
import com.fuar.repository.MessageRepository;
import com.fuar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Get all conversations for a user
     */
    public ConversationListDTO getUserConversations(Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Conversation> conversations = conversationRepository.findConversationsByUserId(userId);
        
        List<ConversationSummaryDTO> conversationSummaries = conversations.stream()
                .map(conversation -> {
                    // Get unread count for this conversation and user
                    List<Message> unreadMessages = messageRepository.findUnreadMessagesInConversation(
                            conversation.getId(), userId);
                    
                    return messageMapper.toConversationSummaryDTO(
                            conversation, 
                            currentUser, 
                            unreadMessages.size());
                })
                .collect(Collectors.toList());
        
        // Get total unread count
        Long totalUnread = conversationRepository.countUnreadMessagesForUser(userId);
        
        return ConversationListDTO.builder()
                .conversations(conversationSummaries)
                .totalUnreadCount(totalUnread.intValue())
                .build();
    }
    
    /**
     * Get a conversation by ID with messages
     */
    public ConversationDTO getConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        
        // Check if user is part of the conversation
        if (!conversation.getInitiator().getId().equals(userId) && 
            !conversation.getRecipient().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not part of this conversation");
        }
        
        // Get messages in conversation
        List<Message> messages = messageRepository.findByConversation_IdOrderByTimestampAsc(conversationId);
        
        // Get unread count
        List<Message> unreadMessages = messageRepository.findUnreadMessagesInConversation(conversationId, userId);
        
        return messageMapper.toConversationDTO(conversation, messages, unreadMessages.size());
    }
    
    /**
     * Get or create a conversation between two users
     */
    @Transactional
    public ConversationDTO getOrCreateConversation(Long initiatorId, Long recipientId) {
        if (initiatorId.equals(recipientId)) {
            throw new IllegalArgumentException("Cannot create conversation with yourself");
        }
        
        User initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiator user not found with id: " + initiatorId));
        
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient user not found with id: " + recipientId));
        
        // Check if conversation already exists
        Conversation conversation = conversationRepository.findConversationBetweenUsers(initiatorId, recipientId)
                .orElseGet(() -> {
                    // Create new conversation
                    Conversation newConversation = Conversation.builder()
                            .initiator(initiator)
                            .recipient(recipient)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    
                    return conversationRepository.save(newConversation);
                });
        
        // Get messages
        List<Message> messages = messageRepository.findByConversation_IdOrderByTimestampAsc(conversation.getId());
        
        // Get unread count
        List<Message> unreadMessages = messageRepository.findUnreadMessagesInConversation(
                conversation.getId(), initiatorId);
        
        return messageMapper.toConversationDTO(conversation, messages, unreadMessages.size());
    }
    
    /**
     * Send a message in a conversation
     */
    @Transactional
    public MessageDTO sendMessage(Long senderId, Long recipientId, String content) {
        try {
            // Log the request
            System.out.println("Sending message: senderId=" + senderId + ", recipientId=" + recipientId);
            
            // Get or create conversation
            ConversationDTO conversationDTO = getOrCreateConversation(senderId, recipientId);
            Conversation conversation = conversationRepository.findById(conversationDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            
            User sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sender user not found with id: " + senderId));
            
            // Create message
            Message message = Message.builder()
                    .conversation(conversation)
                    .sender(sender)
                    .content(content)
                    .timestamp(LocalDateTime.now())
                    .read(false)
                    .build();
            
            // Log message details before saving
            System.out.println("Message to save: senderId=" + message.getSender().getId() + 
                              ", conversationId=" + message.getConversation().getId() + 
                              ", content=" + message.getContent());
            
            // Save message explicitly first in a separate transaction
            try {
                Message savedMessage = messageRepository.saveAndFlush(message);
                System.out.println("Message saved with ID: " + savedMessage.getId());
                
                // Update conversation's updatedAt timestamp and save
                conversation.setUpdatedAt(LocalDateTime.now());
                conversationRepository.save(conversation);
                System.out.println("Conversation updated with ID: " + conversation.getId());
                
                // Convert to DTO
                MessageDTO messageDTO = messageMapper.toMessageDTO(savedMessage);
                
                // Send WebSocket notification
                sendMessageNotification(senderId, recipientId, messageDTO);
                
                return messageDTO;
            } catch (Exception e) {
                System.err.println("Database error saving message: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to save message to database", e);
            }
        } catch (Exception e) {
            System.err.println("Error in sendMessage: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Mark all messages in a conversation as read for a user
     */
    @Transactional
    public int markMessagesAsRead(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        
        // Check if user is part of the conversation
        if (!conversation.getInitiator().getId().equals(userId) && 
            !conversation.getRecipient().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not part of this conversation");
        }
        
        // Find the other user in the conversation
        Long otherUserId = conversation.getInitiator().getId().equals(userId) 
                ? conversation.getRecipient().getId() 
                : conversation.getInitiator().getId();
        
        // Mark messages as read
        List<Message> unreadMessages = messageRepository.findUnreadMessagesInConversation(conversationId, userId);
        int count = unreadMessages.size();
        
        if (count > 0) {
            int updatedCount = messageRepository.markMessagesAsRead(conversationId, userId);
            
            // Send read receipt notification
            sendReadNotification(userId, otherUserId, conversationId, count);
            
            return updatedCount;
        }
        
        return 0;
    }
    
    /**
     * Get the other user in a conversation
     */
    public Long getOtherUserInConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
        
        // Check if user is part of the conversation
        if (conversation.getInitiator().getId().equals(userId)) {
            return conversation.getRecipient().getId();
        } else if (conversation.getRecipient().getId().equals(userId)) {
            return conversation.getInitiator().getId();
        } else {
            throw new IllegalArgumentException("User is not part of this conversation");
        }
    }

    /**
     * Send WebSocket notification for new message
     */
    private void sendMessageNotification(Long senderId, Long recipientId, MessageDTO message) {
        try {
            System.out.println("-------------------------------------");
            System.out.println("URGENT: Preparing to send WebSocket notification to recipient: " + recipientId);
            System.out.println("Message data: conversationId=" + message.getConversationId() + ", sender=" + message.getSender().getId());
            System.out.println("Content: " + message.getContent());
            System.out.println("Timestamp: " + System.currentTimeMillis());
            
            // Validate message data to ensure it has all required fields
            if (message.getConversationId() == null) {
                System.err.println("ERROR: Message is missing conversationId!");
                System.err.println("Attempting to fix missing conversationId in notification");
            }
            
            if (message.getSender() == null) {
                System.err.println("ERROR: Message sender is null!");
                return;
            }
            
            // Try all possible destination patterns to ensure delivery
            try {
                // Method 1: Standard Spring user destination
                messagingTemplate.convertAndSendToUser(
                        recipientId.toString(),
                        "/queue/messages",
                        message
                );
                System.out.println("Message sent via standard pattern to: /user/" + recipientId + "/queue/messages");
                
                // Method 2: Direct queue destination
                messagingTemplate.convertAndSend(
                        "/queue/messages/" + recipientId,
                        message
                );
                System.out.println("Message sent via direct queue to: /queue/messages/" + recipientId);
                
                // Method 3: Topic destination
                messagingTemplate.convertAndSend(
                        "/topic/messages/" + recipientId,
                        message
                );
                System.out.println("Message sent via topic to: /topic/messages/" + recipientId);
                
                System.out.println("WebSocket notification IMMEDIATELY sent via multiple channels");
                System.out.println("Delivery timestamp: " + System.currentTimeMillis());
                
            } catch (Exception e) {
                System.err.println("URGENT: Failed to send WebSocket notification: " + e.getMessage());
                e.printStackTrace();
                
                // Retry once with a small delay
                try {
                    Thread.sleep(50);
                    messagingTemplate.convertAndSendToUser(
                            recipientId.toString(),
                            "/queue/messages",
                            message
                    );
                    System.out.println("Second attempt successful!");
                } catch (Exception retryEx) {
                    System.err.println("Second attempt also failed: " + retryEx.getMessage());
                    System.err.println("Message will not be delivered via WebSocket. Client will need to fetch it via REST API.");
                }
            }
            System.out.println("-------------------------------------");
        } catch (Exception e) {
            System.err.println("Error in sendMessageNotification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send WebSocket notification for read messages
     */
    private void sendReadNotification(Long readerId, Long notifyUserId, Long conversationId, int count) {
        Map<String, Object> readData = new HashMap<>();
        readData.put("conversationId", conversationId);
        readData.put("readerId", readerId);
        readData.put("count", count);
        
        messagingTemplate.convertAndSendToUser(
                notifyUserId.toString(),
                "/queue/read",
                readData
        );
    }
}
