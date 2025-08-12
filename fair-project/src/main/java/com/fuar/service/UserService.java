package com.fuar.service;

import com.fuar.model.Role;
import com.fuar.model.User;
import com.fuar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Kullanıcı ile ilgili işlemleri yöneten servis sınıfı
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    
    /**
     * Kullanıcı adına göre kullanıcı getirir (email adresi kullanıcı adı olarak kullanılır)
     * @param username Kullanıcı adı (email)
     * @return Kullanıcı nesnesi
     */
    public User findUserByUsername(String username) {
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("Bu kullanıcı adıyla kullanıcı bulunamadı: " + username));
    }

    /**
     * Kullanıcı ID'sine göre kullanıcı getirir
     * @param id Kullanıcı ID'si
     * @return Kullanıcı nesnesi (varsa)
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + id));
    }

    /**
     * Email adresine göre kullanıcı getirir
     * @param email Kullanıcı email adresi
     * @return Kullanıcı nesnesi (varsa)
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Bu email ile kullanıcı bulunamadı: " + email));
    }

    /**
     * Tüm kullanıcıları sayfalandırılmış şekilde getirir
     * @param pageable Sayfalandırma bilgisi
     * @return Sayfalandırılmış kullanıcı listesi
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Belirli bir role sahip kullanıcıları getirir
     * @param role Kullanıcı rolü
     * @param pageable Sayfalandırma bilgisi
     * @return Sayfalandırılmış kullanıcı listesi
     */
    public Page<User> getUsersByRole(String roleStr, Pageable pageable) {
        try {
            Role role = Role.valueOf(roleStr);
            return userRepository.findByRole(role, pageable);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Geçersiz rol: " + roleStr);
        }
    }

    /**
     * Kullanıcı kaydeder veya günceller
     * @param user Kaydedilecek/güncellenecek kullanıcı
     * @return Kaydedilen kullanıcı
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Kullanıcı siler
     * @param id Silinecek kullanıcı ID'si
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
