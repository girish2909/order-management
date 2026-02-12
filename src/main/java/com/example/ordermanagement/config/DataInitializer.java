package com.example.ordermanagement.config;

import com.example.ordermanagement.entity.User;
import com.example.ordermanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin")) // Encode the password
                        .email("admin@example.com")
                        .roles(new HashSet<>(Collections.singletonList("ADMIN")))
                        .active(true)
                        .build();
                userRepository.save(admin);
                System.out.println("Admin user created: username=admin, password=admin");
            }

            if (userRepository.findByUsername("user").isEmpty()) {
                User user = User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("password")) // Encode the password
                        .email("user@example.com")
                        .roles(new HashSet<>(Collections.singletonList("USER")))
                        .active(true)
                        .build();
                userRepository.save(user);
                System.out.println("Standard user created: username=user, password=password");
            }
        };
    }
}