package com.fuar.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * Lenient WebSocket interceptor for development purposes
 * This interceptor allows all WebSocket connections without authentication
 */
@Component
@RequiredArgsConstructor
public class DevWebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            System.out.println("DEV MODE: Allowing all WebSocket connections without authentication");
            
            // Log headers for debugging
            System.out.println("WebSocket connection headers: " + accessor.toNativeHeaderMap());
            
            // Set a default user principal for development
            accessor.setUser(() -> "dev-user");
        }
        
        return message;
    }
}
