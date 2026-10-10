package SmartHomeServices.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SmartHomeServices.dto.AuthResponse;
import SmartHomeServices.dto.LoginRequest;
import SmartHomeServices.dto.RegisterRequest;
import SmartHomeServices.entity.User;
import SmartHomeServices.repository.UserRepository;
import SmartHomeServices.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthController(
            UserRepository userRepository,
            JwtUtil jwtUtil) {

        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/test")
    public String testAuth() {
        return "Auth API is working";
    }

    @PostMapping("/register")
    public AuthResponse register(
            @RequestBody RegisterRequest request) {

        if (userRepository
                .findByEmail(request.getEmail())
                .isPresent()) {

            return new AuthResponse(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Email already registered"
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setPhone(request.getPhone());

        /*
         * Get the role selected during registration.
         *
         * If no role is provided, create a CUSTOMER account.
         */
        String role = request.getRole();

        if (role == null || role.isBlank()) {
            role = "CUSTOMER";
        }

        role = role.toUpperCase();

        /*
         * Only CUSTOMER and PROVIDER are allowed
         * through public registration.
         */
        if (!role.equals("CUSTOMER") &&
            !role.equals("PROVIDER")) {

            return new AuthResponse(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Invalid role selected"
            );
        }

        user.setRole(role);

        User savedUser = userRepository.save(user);

        /*
         * Generate JWT immediately after successful
         * registration.
         *
         * This allows a newly registered provider to
         * securely save their provider details.
         */
        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole()
        );

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getRole(),
                token,
                "Registration successful"
        );
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {

            return new AuthResponse(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Invalid email or password"
            );
        }

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {

            return new AuthResponse(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Invalid email or password"
            );
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                token,
                "Login successful"
        );
    }
}