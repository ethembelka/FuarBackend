package com.fuar.service;

import com.fuar.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendPrivateMessage(Long userId, MessageDTO message) {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/messages",
            message
        );
    }

    public void sendToTopic(String topic, Object message) {
        messagingTemplate.convertAndSend("/topic/" + topic, message);
    }
}
