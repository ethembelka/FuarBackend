package com.fuar.mapper;

import com.fuar.dto.ConversationDTO;
import com.fuar.dto.ConversationSummaryDTO;
import com.fuar.dto.MessageDTO;
import com.fuar.dto.UserDTO;
import com.fuar.model.Conversation;
import com.fuar.model.Message;
import com.fuar.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageMapper {

    /**
     * Convert a Message entity to MessageDTO
     */
    public MessageDTO toMessageDTO(Message message) {
        if (message == null) {
            return null;
        }

        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .sender(toUserDTO(message.getSender()))
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .read(message.isRead())
                .build();
    }

    /**
     * Convert a Conversation entity to ConversationDTO with messages
     */
    public ConversationDTO toConversationDTO(Conversation conversation, List<Message> messages, int unreadCount) {
        if (conversation == null) {
            return null;
        }

        List<MessageDTO> messageDTOs = messages.stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());

        // Get the last message if there are any
        MessageDTO lastMessage = messages.isEmpty() ? null : 
                toMessageDTO(messages.get(messages.size() - 1));

        return ConversationDTO.builder()
                .id(conversation.getId())
                .initiator(toUserDTO(conversation.getInitiator()))
                .recipient(toUserDTO(conversation.getRecipient()))
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .messages(messageDTOs)
                .build();
    }

    /**
     * Convert a Conversation entity to ConversationSummaryDTO
     */
    public ConversationSummaryDTO toConversationSummaryDTO(Conversation conversation, User currentUser, int unreadCount) {
        if (conversation == null) {
            return null;
        }

        User otherUser = conversation.getInitiator().getId().equals(currentUser.getId()) 
                ? conversation.getRecipient() 
                : conversation.getInitiator();

        // Get last message content and timestamp
        String lastMessageContent = null;
        if (!conversation.getMessages().isEmpty()) {
            Message lastMessage = conversation.getMessages().get(conversation.getMessages().size() - 1);
            lastMessageContent = lastMessage.getContent();
        }

        return ConversationSummaryDTO.builder()
                .id(conversation.getId())
                .initiator(toUserDTO(conversation.getInitiator()))
                .recipient(toUserDTO(conversation.getRecipient()))
                .otherUser(toUserDTO(otherUser))
                .updatedAt(conversation.getUpdatedAt())
                .lastMessageContent(lastMessageContent)
                .lastMessageTimestamp(conversation.getUpdatedAt())
                .unreadCount(unreadCount)
                .build();
    }

    /**
     * Convert a User entity to UserDTO
     */
    private UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .image(user.getImage())
                .build();
    }
}
