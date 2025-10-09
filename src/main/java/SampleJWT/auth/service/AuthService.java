package SampleJWT.auth.service;

import SampleJWT.auth.dto.LoginDTO;
import SampleJWT.auth.dto.RegisterDTO;
import SampleJWT.auth.entity.User;
import SampleJWT.auth.repository.UserRepository;
import SampleJWT.auth.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtil jwtUtil;

    // ✅ Register new user
    public String registerUser(RegisterDTO registerDTO) {
        if (userRepository.findByUsername(registerDTO.getUsername()).isPresent()) {
            return "Username already exists!";
        }

        User newUser = new User();
        newUser.setFirstname(registerDTO.getFirstname());
        newUser.setLastname(registerDTO.getLastname());
        newUser.setEmail(registerDTO.getEmail());
        newUser.setUsername(registerDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        newUser.setRole("ROLE_USER");

        userRepository.save(newUser);
        return "User registered successfully!";
    }

    // ✅ Login user and return JWT token
    public String loginUser(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                // Token valid for 15 minutes
                return jwtUtil.generateToken(loginDTO.getUsername(), 15);
            } else {
                return "Invalid login attempt!";
            }
        } catch (Exception e) {
            return "Invalid username or password!";
        }
    }
}
