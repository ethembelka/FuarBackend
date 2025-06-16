package com.fuar.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**                         
 * WebSocket Kimlik Doğrulama Interceptor'ı
 * 
 * Bu interceptor, bağlantı isteği başlıklarına dahil edilen JWT token'ını doğrulayarak
 * WebSocket bağlantılarını doğrular. Yalnızca kimliği doğrulanmış kullanıcıların
 * WebSocket bağlantıları kurabilmesini sağlar.
 * 
 * Bu interceptor şunları yapar:
 * - STOMP başlıklarından JWT token'ını çıkarır
 * - JwtService kullanarak token'ı doğrular
 * - WebSocket oturumu için kimliği doğrulanmış kullanıcının principal'ini ayarlar
 * - Kimlik doğrulama başarısız olursa erişimi reddeder
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Gönderilmeden önce WebSocket mesajlarını yakalar.
     * CONNECT komutları için kimlik doğrulama token'ını doğrular.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                // Hata ayıklama için başlıkları kaydet
                System.out.println("-------------------------------------");
                System.out.println("WebSocket bağlantı girişimi, başlıklar: " + accessor.toNativeHeaderMap());
                
                String authToken = accessor.getFirstNativeHeader("Authorization");
                System.out.println("Authorization başlığı: " + (authToken != null ? "var" : "yok"));
                
                if (authToken != null && authToken.startsWith("Bearer ")) {
                    try {
                        String token = authToken.substring(7);
                        String username = jwtService.extractUsername(token);
                        System.out.println("Token'dan çıkarılan kullanıcı adı: " + username);
                        
                        var userDetails = userDetailsService.loadUserByUsername(username);
                        
                        if (jwtService.isTokenValid(token, userDetails)) {
                            System.out.println("Token, kullanıcı için geçerli: " + username);
                            
                            // Eğer userDetails bir User varlığıysa, ID'sini çıkar
                            if (userDetails instanceof org.springframework.security.core.userdetails.User) {
                                // Spring'in User'ı için, principal olarak kullanıcı adını ayarla
                                accessor.setUser(() -> userDetails.getUsername());
                                System.out.println("WebSocket principal'ini kullanıcı adıyla ayarla: " + userDetails.getUsername());
                            } else if (userDetails instanceof com.fuar.model.User) {
                                // Kendi User varlığımız için, erişimi kolaylaştırmak amacıyla principal olarak ID'yi ayarla
                                Long userId = ((com.fuar.model.User) userDetails).getId();
                                
                                // ÖNEMLİ: Principal, convertAndSendToUser'da kullanılanla tam olarak eşleşmelidir
                                accessor.setUser(() -> userId.toString());
                                System.out.println("WebSocket principal'ini kullanıcı ID'siyle ayarla: " + userId);
                                
                                // Ayrıca, kolay erişim için oturum niteliklerini ayarla
                                accessor.getSessionAttributes().put("userId", userId);
                                accessor.getSessionAttributes().put("username", username);
                                
                                // Mesaj yönlendirmesine yardımcı olması için ek nitelikler
                                accessor.getSessionAttributes().put("userIdString", userId.toString());
                                accessor.getSessionAttributes().put("authenticated", true);
                                
                                // Kullanıcının hangi abonelik deseninde mesaj alacağını doğrulamak için hata ayıklama kaydı
                                System.out.println("Bu kullanıcı /user/" + userId + "/queue/messages adresinden mesaj alacak");
                                System.out.println("Bu kullanıcı /queue/messages/" + userId + " adresinden de mesaj alacak");
                                System.out.println("Bu kullanıcı /topic/messages/" + userId + " adresinden de mesaj alacak");
                            } else {
                                // Yedek olarak kullanıcı adına dön
                                accessor.setUser(() -> username);
                                System.out.println("Yedek kullanıcı adıyla WebSocket principal'ini ayarla: " + username);
                            }
                            
                            System.out.println("WebSocket kimlik doğrulama başarılı");
                            System.out.println("-------------------------------------");
                            return message;
                        } else {
                            System.err.println("Kullanıcı adı için token doğrulama başarısız: " + username);
                        }
                    } catch (Exception e) {
                        System.err.println("WebSocket kimlik doğrulama sırasında hata: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("WebSocket bağlantısı için Authorization başlığı eksik veya geçersiz");
                    // Geliştirme sırasında hata ayıklama için, kimlik doğrulama olmadan bağlantılara izin ver
                    System.out.println("HATA AYIKLAMA MODU: Geliştirme için kimlik doğrulamasız WebSocket bağlantısına izin veriliyor");
                    accessor.setUser(() -> "anonymous-dev-user");
                    System.out.println("Geliştirme modunda WebSocket bağlantısı için anonim kullanıcı ayarlandı");
                    System.out.println("-------------------------------------");
                    return message;
                }
                
                // Buraya gelinirse, kimlik doğrulama başarısız olmuştur
                System.err.println("WebSocket kimlik doğrulama başarısız - bağlantı reddedilecek");
                System.out.println("-------------------------------------");
                throw new AccessDeniedException("WebSocket kimlik doğrulama başarısız");
            } catch (Exception e) {
                System.err.println("WebSocket kimlik doğrulama sırasında beklenmeyen hata: " + e.getMessage());
                e.printStackTrace();
                throw new AccessDeniedException("WebSocket kimlik doğrulama hatası", e);
            }
        }
        
        // CONNECT dışındaki çerçeveler için, sadece iletmeye devam et
        return message;
    }
}
