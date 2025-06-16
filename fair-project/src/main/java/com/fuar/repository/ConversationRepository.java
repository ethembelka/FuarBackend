package com.fuar.repository;

import com.fuar.model.Conversation;
import com.fuar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    /**
     * Find all conversations where a user is either the initiator or recipient
     * @param userId The user ID
     * @return List of conversations
     */
    @Query("SELECT c FROM Conversation c WHERE c.initiator.id = :userId OR c.recipient.id = :userId " +
           "ORDER BY c.updatedAt DESC")
    List<Conversation> findConversationsByUserId(@Param("userId") Long userId);
    
    /**
     * Find a conversation between two users
     * @param user1Id First user ID
     * @param user2Id Second user ID
     * @return Optional conversation if exists
     */
    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.initiator.id = :user1Id AND c.recipient.id = :user2Id) OR " +
           "(c.initiator.id = :user2Id AND c.recipient.id = :user1Id)")
    Optional<Conversation> findConversationBetweenUsers(
            @Param("user1Id") Long user1Id, 
            @Param("user2Id") Long user2Id);
    
    /**
     * Count unread messages for a user
     * @param userId The user ID
     * @return Count of unread messages
     */
    @Query("SELECT COUNT(m) FROM Conversation c JOIN c.messages m " +
           "WHERE (c.initiator.id = :userId OR c.recipient.id = :userId) " +
           "AND m.sender.id <> :userId AND m.read = false")
    Long countUnreadMessagesForUser(@Param("userId") Long userId);
}
