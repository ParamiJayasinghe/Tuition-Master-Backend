package com.tuition.backend.Controller;

import com.tuition.backend.Security.JwtService;
import com.tuition.backend.dto.LoginRequestDto;
import com.tuition.backend.dto.LoginResponseDto;
import com.tuition.backend.Entity.User;
import com.tuition.backend.Repository.userRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private userRepository userRepo;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
//        claims.put("userId", user.getId());

        String token = jwtService.generateToken(claims, user.getUsername());

        return ResponseEntity.ok(
                new LoginResponseDto(
                        token,
                        user.getUsername(),
                        user.getRole()
                )
        );
    }
}
