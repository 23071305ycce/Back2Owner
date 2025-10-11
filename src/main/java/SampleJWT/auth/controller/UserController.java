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
@RequestMapping
public class UserController {

    @Autowired
    private AuthService authService;

                        //<- USER ROLE ->//
    //User Register
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterDTO registerDTO) {
        String response = authService.registerUser(registerDTO);
        if (response.contains("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    //User Login
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDTO) {
        String token = authService.loginUser(loginDTO);
        if (token.startsWith("Invalid") || token.startsWith("Username")) {
            return ResponseEntity.badRequest().body(token);
        }

        return ResponseEntity.ok()
                .body("Login successful! Token: " + token + "\nExpires in 15 minutes");
    }

    //if the users jwt token expires
    @GetMapping("/session-expired")
    public ResponseEntity<String> sessionExpired() {
        return ResponseEntity.status(401).body("Session expired! Please login again.");
    }

    //Get user by id
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        UserDTO userDTO = authService.getUserById(id);
        if (userDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDTO);
    }

    //update the user deatils
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody UserUpdateDTO updateDTO,
                                        Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String requester = authentication.getName();
        try {
            UserDTO updated = authService.updateUser(id, updateDTO, requester);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            return ResponseEntity.status(403).body("Forbidden");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String username = authentication.getName();
        try {
            UserDTO userDTO = authService.getUserByUsername(username);
            if (userDTO == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch user");
        }
    }

                            //<- ADMIN ROLE ->//
    //get all user
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> listUsers() {
        List<UserDTO> users = authService.listAllUsers();
        return ResponseEntity.ok(users);
    }

    //update any user's role
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        authService.updateUserRole(id, role);
        return ResponseEntity.ok("Role updated");
    }
}
