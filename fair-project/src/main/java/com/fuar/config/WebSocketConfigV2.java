package com.fuar.config;

import com.fuar.security.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Konfigürasyonu
 * 
 * Bu konfigürasyon sınıfı, STOMP protokolünü kullanarak WebSocket mesajlaşma altyapısını yapılandırır.
 * Aşağıdaki ayarları yapar:
 * - WebSocket bağlantıları için STOMP endpoint'leri
 * - Mesaj broker hedefleri
 * - Kimlik doğrulama interceptor'ları
 * - Cross-origin resource sharing (CORS) ayarları
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfigV2 implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * WebSocket bağlantıları için STOMP endpoint'lerini kaydeder.
     * WebSocket desteklemeyen tarayıcılar için SockJS yedek seçenekleri sunar.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println("WebSocketConfigV2 için açık yollarla STOMP endpoint'leri yapılandırılıyor");
        
        // Mevcut istemci kodunu desteklemek için her iki endpoint'i de kaydet
        registry.addEndpoint("/api/v1/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
                
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
                
        System.out.println("WebSocket endpoint'leri kaydedildi: /api/v1/ws ve /ws");
    }

    /**
     * Mesaj broker ayarlarını yapılandırır.
     * İstemci mesajları için hedef önekleri belirler ve basit bir bellek içi broker etkinleştirir.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Anında teslimat için optimize edilmiş ayarlarla mesaj broker'ı yapılandır
        registry.setApplicationDestinationPrefixes("/app")
                .enableSimpleBroker("/topic", "/queue", "/user/queue", "/user")
                // Daha hızlı mesaj iletimi için daha küçük tampon boyutları ve daha hızlı kalp atışı
                .setTaskScheduler(new ConcurrentTaskScheduler())
                .setHeartbeatValue(new long[] {3000, 3000});
                
        // Daha açık bir kullanıcı hedef öneki kullan
        registry.setUserDestinationPrefix("/user");
        
        System.out.println("Mesaj broker'ı anında teslimat için optimize edilmiş ayarlarla yapılandırıldı");
        System.out.println("Kullanıcı hedef öneki açıkça /user olarak ayarlandı");
        System.out.println("Gereken tüm broker önekleri etkinleştirildi: /topic, /queue, /user/queue, /user");
        System.out.println("Daha duyarlı bağlantılar için kalp atışı aralığı 3 saniyeye düşürüldü");
    }

    /**
     * İstemci giriş kanalını yapılandırır.
     * Kanala WebSocket kimlik doğrulama interceptor'ını ekler.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
