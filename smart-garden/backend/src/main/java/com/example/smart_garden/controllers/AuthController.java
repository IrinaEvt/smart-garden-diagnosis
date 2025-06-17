package com.example.smart_garden.controllers;

import com.example.smart_garden.auth.JwtUtil;
import com.example.smart_garden.entities.UserEntity;
import com.example.smart_garden.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {
        if (userRepo.findByUsername(req.get("username")).isPresent()) {
            return ResponseEntity.badRequest().body("Потребителското име е заето");
        }

        UserEntity user = new UserEntity();
        user.setUsername(req.get("username"));
        user.setPassword(passwordEncoder.encode(req.get("password")));
        user.setRole("ROLE_USER");

        userRepo.save(user);
        return ResponseEntity.ok("Регистрация успешна");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        Optional<UserEntity> userOpt = userRepo.findByUsername(req.get("username"));
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Невалидно потребителско име");

        UserEntity user = userOpt.get();
        if (!passwordEncoder.matches(req.get("password"), user.getPassword())) {
            return ResponseEntity.status(401).body("Невалидна парола");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok(Map.of("token", token, "userId", user.getId()));
    }

}

