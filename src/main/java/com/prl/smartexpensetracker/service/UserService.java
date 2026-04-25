package com.prl.smartexpensetracker.service;

import com.prl.smartexpensetracker.entity.User;
import com.prl.smartexpensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Map<String, String> addOrUpdateProperty(String username, String key, String value) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProperties() == null) {
            user.setProperties(new HashMap<>());
        }

        user.getProperties().put(key, value);

        userRepository.save(user);
        return user.getProperties();
    }

    public Map<String, String> removeProperty(String username, String key) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProperties() != null) {
            user.getProperties().remove(key);
        }

        userRepository.save(user);
        return user.getProperties();
    }

    public Map<String, String> getProperties(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getProperties();
    }
}