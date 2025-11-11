package com.tuition.backend.config;

import com.tuition.backend.Entity.User;
import com.tuition.backend.Repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private userRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Map your user.role (e.g., "ADMIN") to a GrantedAuthority like "ROLE_ADMIN"
        String role = user.getRole() != null ? user.getRole().toUpperCase() : "USER";
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())        // must be bcrypt-encoded in DB
                .authorities(Collections.singletonList(authority))
                .disabled(user.getIsActive() == null ? false : !user.getIsActive())
                .build();
    }
}
