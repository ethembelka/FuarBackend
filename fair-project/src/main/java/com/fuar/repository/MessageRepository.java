package com.fuar.repository;

import com.fuar.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Find all messages in a conversation
     * @param conversationId The conversation ID
     * @return List of messages
     */
    List<Message> findByConversation_IdOrderByTimestampAsc(Long conversationId);
    
    /**
     * Find unread messages for a specific user in a conversation
     * @param conversationId The conversation ID
     * @param userId The user ID who receives the messages
     * @return List of unread messages
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
           "AND m.sender.id <> :userId AND m.read = false")
    List<Message> findUnreadMessagesInConversation(
            @Param("conversationId") Long conversationId, 
            @Param("userId") Long userId);
    
    /**
     * Mark all unread messages in a conversation as read for a specific user
     * @param conversationId The conversation ID
     * @param userId The user ID who is marking messages as read
     * @return Number of updated messages
     */
    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.conversation.id = :conversationId " +
           "AND m.sender.id <> :userId AND m.read = false")
    int markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
