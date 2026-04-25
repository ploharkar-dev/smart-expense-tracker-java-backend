package com.prl.smartexpensetracker.controller;

import com.prl.smartexpensetracker.dto.RemovePropertyRequest;
import com.prl.smartexpensetracker.dto.UserPropertyRequest;
import com.prl.smartexpensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ✅ Add / Update property
    @PostMapping("/property")
    public Map<String, String> addProperty(
            @RequestBody UserPropertyRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return userService.addOrUpdateProperty(username, request.getKey(), request.getValue());
    }

    // ✅ Remove property
    @DeleteMapping("/property")
    public Map<String, String> removeProperty(
            @RequestBody RemovePropertyRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        return userService.removeProperty(username, request.getKey());
    }

    // ✅ Get all properties
    @GetMapping("/properties")
    public Map<String, String> getProperties(Authentication authentication) {
        String username = authentication.getName();
        return userService.getProperties(username);
    }
}
