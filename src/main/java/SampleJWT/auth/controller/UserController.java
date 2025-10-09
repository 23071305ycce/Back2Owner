package SampleJWT.auth.controller;

import SampleJWT.auth.dto.LoginDTO;
import SampleJWT.auth.dto.RegisterDTO;
import SampleJWT.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class UserController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterDTO registerDTO) {
        String response = authService.registerUser(registerDTO);
        if (response.contains("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDTO) {
        String token = authService.loginUser(loginDTO);
        if (token.startsWith("Invalid") || token.startsWith("Username")) {
            return ResponseEntity.badRequest().body(token);
        }

        return ResponseEntity.ok()
                .body("Login successful! Token: " + token + "\nExpires in 15 minutes");
    }

    @GetMapping("/session-expired")
    public ResponseEntity<String> sessionExpired() {
        return ResponseEntity.status(401).body("Session expired! Please login again.");
    }
}
