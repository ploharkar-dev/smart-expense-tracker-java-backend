package com.prl.smartexpensetracker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.prl.smartexpensetracker.dto.LoginRequest;
import com.prl.smartexpensetracker.dto.LoginResponse;
import com.prl.smartexpensetracker.dto.RegisterRequest;
import com.prl.smartexpensetracker.dto.RegisterResponse;
import com.prl.smartexpensetracker.entity.Category;
import com.prl.smartexpensetracker.entity.User;
import com.prl.smartexpensetracker.exception.InvalidInputException;
import com.prl.smartexpensetracker.exception.ResourceNotFoundException;
import com.prl.smartexpensetracker.repository.UserRepository;
import com.prl.smartexpensetracker.repository.CategoryRepository;
import com.prl.smartexpensetracker.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    /**
     * Register a new user.
     *
     * @param registerRequest Registration request with username, email, password
     * @return RegisterResponse with user details
     */
    public RegisterResponse register(RegisterRequest registerRequest) {
        // Validation
        if (registerRequest.getUsername() == null || registerRequest.getUsername().isEmpty()) {
            throw new InvalidInputException("Username cannot be empty");
        }
        if (registerRequest.getEmail() == null || registerRequest.getEmail().isEmpty()) {
            throw new InvalidInputException("Email cannot be empty");
        }
        if (registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty()) {
            throw new InvalidInputException("Password cannot be empty");
        }
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new InvalidInputException("Passwords do not match");
        }

        // Check if user already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new InvalidInputException("Username already exists");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new InvalidInputException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .properties(Map.of("monthlyBudget", "10000"))
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        
        createDefaultCategories(savedUser);

        return RegisterResponse.builder()
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .message("User registered successfully")
                .build();
    }

    /**
     * Login user and return JWT token.
     *
     * @param loginRequest Login request with username and password
     * @return LoginResponse with JWT token
     */
    public LoginResponse login(LoginRequest loginRequest) {
        // Validation
        if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()) {
            throw new InvalidInputException("Username cannot be empty");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            throw new InvalidInputException("Password cannot be empty");
        }

        // Find user by username
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new ResourceNotFoundException("Invalid credentials");
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userId(user.getUserId())
                .properties(user.getProperties())
                .message("Login successful")
                .build();
    }

    /**
     * Find user by username.
     *
     * @param username Username
     * @return User object
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Find user by ID.
     *
     * @param userId User ID
     * @return User object
     */
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    private void createDefaultCategories(User user) {
        List<Category> categories = List.of(
                Category.builder()
                        .categoryName("Food & Dining")
                        .description("Food expenses")
                        .createdAt(LocalDateTime.now())
                        .user(user)
                        .build(),

                Category.builder()
                        .categoryName("Travel")
                        .description("Travel expenses")
                        .createdAt(LocalDateTime.now())
                        .user(user)
                        .build(),

                Category.builder()
                        .categoryName("Utilities")
                        .description("Bills")
                        .createdAt(LocalDateTime.now())
                        .user(user)
                        .build(),

                Category.builder()
                        .categoryName("Entertainment")
                        .description("Fun")
                        .createdAt(LocalDateTime.now())
                        .user(user)
                        .build(),

                Category.builder()
                        .categoryName("Shopping")
                        .description("Shopping")
                        .createdAt(LocalDateTime.now())
                        .user(user)
                        .build(),

                Category.builder()
                        .categoryName("Other")
                        .description("Other")
                        .createdAt(LocalDateTime.now())
                        .user(user)
                        .build()
        );

        categoryRepository.saveAll(categories);
    }
}
