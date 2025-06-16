package com.fuar.config;

import com.fuar.security.DevWebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Development-only WebSocket configuration with lenient authentication
 * Only activate this in development environments
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class DevWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final DevWebSocketAuthInterceptor devWebSocketAuthInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println("DEV MODE: Configuring STOMP endpoints with lenient authentication");
        
        // Register both endpoints to support any client configuration
        registry.addEndpoint("/api/v1/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
                
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
                
        System.out.println("DEV MODE: Registered WebSocket endpoints: /api/v1/ws and /ws");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app")
                .enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(devWebSocketAuthInterceptor);
    }
}
