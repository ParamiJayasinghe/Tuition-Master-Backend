package com.tuition.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security Configuration
 * 
 * This class configures Spring Security for the application:
 * 1. Authentication: How users log in (HTTP Basic Auth)
 * 2. Authorization: Who can access which endpoints
 * 3. Password Encoding: BCrypt for secure password storage
 * 4. CORS: Allows frontend (React) to communicate with backend
 * 5. Method Security: Enables @PreAuthorize annotations in controllers/services
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize and @PostAuthorize annotations
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Authentication Provider
     * 
     * Tells Spring Security HOW to authenticate users:
     * - Uses our CustomUserDetailsService to load users from database
     * - Uses BCryptPasswordEncoder to verify passwords
     * 
     * When a user tries to log in:
     * 1. Spring calls CustomUserDetailsService.loadUserByUsername()
     * 2. Gets the user from database
     * 3. Compares provided password (hashed) with stored password (hashed)
     * 4. If match, user is authenticated
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Security Filter Chain
     * 
     * This is the MAIN security configuration that defines:
     * - Which URLs require authentication
     * - Which URLs are public (permitAll)
     * - How authentication works (HTTP Basic)
     * - CORS settings for frontend communication
     * 
     * Order matters! More specific rules should come first.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF (Cross-Site Request Forgery) Protection
                // Disabled for REST APIs (stateless) - we use tokens/auth headers instead
                .csrf(csrf -> csrf.disable())
                
                // CORS Configuration - Allows React frontend to call this backend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Authorization Rules - Who can access what?
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ENDPOINTS - No authentication required
                        .requestMatchers("/api/users").permitAll()           // Create user (registration)
                        .requestMatchers("/api/users/**").permitAll()        // All user endpoints (for now)
                        
                        // PROTECTED ENDPOINTS - Require authentication
                        // Note: Role checks are done in Service layer (TeacherService, StudentService)
                        // Here we just ensure user is logged in
                        .requestMatchers("/api/teachers/**").authenticated()  // Must be logged in
                        .requestMatchers("/api/students/**").authenticated()  // Must be logged in
                        
                        // DEFAULT: All other requests require authentication
                        .anyRequest().authenticated()
                )
                
                // Authentication Method: HTTP Basic Auth
                // User sends username:password in Authorization header
                // Format: "Authorization: Basic base64(username:password)"
                .httpBasic();
        
        // Register our custom authentication provider
        http.authenticationProvider(authenticationProvider());
        
        return http.build();
    }

    /**
     * CORS Configuration
     * 
     * CORS = Cross-Origin Resource Sharing
     * 
     * Problem: Browser blocks requests from one domain (React: localhost:3000) 
     *          to another domain (Backend: localhost:8080) for security
     * 
     * Solution: Configure backend to allow requests from frontend
     * 
     * This allows:
     * - React app (localhost:3000) to call this backend (localhost:8080)
     * - Specific HTTP methods (GET, POST, PUT, DELETE)
     * - Specific headers (Authorization, Content-Type)
     * - Credentials (cookies, auth headers)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow requests from React frontend (adjust port if different)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // React dev server
                "http://localhost:5173",  // Vite dev server (if using Vite)
                "http://127.0.0.1:3000"
        ));
        
        // Allow these HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow these headers (Authorization for Basic Auth, Content-Type for JSON)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Password Encoder
     * 
     * BCrypt is a one-way hashing algorithm:
     * - Password "admin123" → Hashed: "$2a$10$..."
     * - Same password always produces different hash (salt added automatically)
     * - Cannot reverse hash to get original password
     * - When user logs in, we hash their input and compare with stored hash
     * 
     * Why BCrypt?
     * - Industry standard
     * - Slow by design (prevents brute force attacks)
     * - Automatically handles salt (random data added to password before hashing)
     * 
     * Example:
     * - User creates account with password "admin123"
     * - We hash it: passwordEncoder.encode("admin123") → "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
     * - Store hash in database
     * - When user logs in with "admin123", we hash it again and compare
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
