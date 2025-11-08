package com.tuition.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // disable CSRF for testing API calls (Postman/curl). Re-enable for browser forms in production.
                .csrf(csrf -> csrf.disable())

                // authorize requests
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users").permitAll()   // allow POST /api/users (and GET if used)
                        .requestMatchers("/api/users/**").permitAll() // optionally permit subpaths if needed
                        .anyRequest().authenticated()
                )

                // default httpBasic (you can remove or replace with formLogin/jwt later)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // Password encoder bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
