package SampleJWT.auth.controller;

import SampleJWT.auth.dto.LoginDTO;
import SampleJWT.auth.dto.RegisterDTO;
import SampleJWT.auth.dto.UserDTO;
import SampleJWT.auth.dto.UserUpdateDTO;
import SampleJWT.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private AuthService authService;

    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterDTO registerDTO) {
        try {
            String response = authService.registerUser(registerDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginDTO loginDTO) {
        String token = authService.loginUser(loginDTO);
        if (token.startsWith("Invalid")) {
            return ResponseEntity.status(401).body(token);
        }
        return ResponseEntity.ok("Token: " + token);
    }

    // Get current user info
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(401).build();
        }
        String username = auth.getName();
        UserDTO user = authService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    // Get user by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        UserDTO user = authService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    // Update user
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String id,
            @RequestBody UserUpdateDTO dto,
            Authentication auth
    ) {
        if (auth == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String requesterUsername = auth.getName();
            UserDTO updated = authService.updateUser(id, dto, requesterUsername);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // List all users (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> listAllUsers() {
        List<UserDTO> users = authService.listAllUsers();
        return ResponseEntity.ok(users);
    }

    // Update user role (admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<String> updateUserRole(
            @PathVariable String id,
            @RequestParam String role
    ) {
        try {
            authService.updateUserRole(id, role);
            return ResponseEntity.ok("Role updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
