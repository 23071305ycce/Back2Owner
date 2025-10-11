package SampleJWT.auth.service;

import SampleJWT.auth.dto.LoginDTO;
import SampleJWT.auth.dto.RegisterDTO;
import SampleJWT.auth.dto.UserDTO;
import SampleJWT.auth.dto.UserUpdateDTO;
import SampleJWT.auth.entity.User;
import SampleJWT.auth.repository.UserRepository;
import SampleJWT.auth.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

                            //<- USER SERVICES ->//
    //for /register
    public String registerUser(RegisterDTO registerDTO) {
        if (userRepository.findByUsername(registerDTO.getUsername()).isPresent()) {
            return "Username already exists!";
        }

        //mapping :- RegisterDTO to User
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

    //for /login
    public String loginUser(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                return jwtUtil.generateToken(loginDTO.getUsername());
            } else {
                return "Invalid login attempt!";
            }
        } catch (Exception e) {
            return "Invalid username or password!";
        }
    }

    //for /users/{id}
    public UserDTO getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id.intValue());
        if (userOpt.isEmpty()) {
            return null;
        }
        User user = userOpt.get();
        return new UserDTO(
                (int) user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getUsername(),
                user.getRole()
        );
    }

    //update the user
    public UserDTO updateUser(Long id, UserUpdateDTO dto, String requesterUsername) {
        User user = userRepository.findById(id.intValue())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getUsername().equals(requesterUsername)) {
            throw new AccessDeniedException("Not allowed to update this user");
        }

        if (dto.getFirstname() != null) user.setFirstname(dto.getFirstname());
        if (dto.getLastname() != null) user.setLastname(dto.getLastname());

        //valid mail && is the current email different
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            //does the new mail already exits in the repo
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(dto.getEmail());
        }

        // Keep this block only if username is allowed to change
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new IllegalArgumentException("Username already in use");
            }
            user.setUsername(dto.getUsername());
        }

        userRepository.save(user);

        return new UserDTO(
                (int) user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getUsername(),
                user.getRole()
        );
    }

    // for /me
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new UserDTO(
                (int) user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getUsername(),
                user.getRole()
        );
    }

                            //<- ADMIN SERVICES ->//
    // for /users
    public List<UserDTO> listAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(u -> new UserDTO(
                        (int) u.getId(),
                        u.getFirstname(),
                        u.getLastname(),
                        u.getEmail(),
                        u.getUsername(),
                        u.getRole()
                ))
                .collect(Collectors.toList());
    }

    //to convert user's role to admin
    public void updateUserRole(Long id, String role) {
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_USER".equals(role)) {
            throw new IllegalArgumentException("Invalid role");
        }
        User user = userRepository.findById(id.intValue())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }
}
