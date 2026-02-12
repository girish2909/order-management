package com.example.ordermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for Web Security and CORS settings.
 * <p>
 * This class configures the security filter chain, user details service for authentication, and Cross-Origin Resource
 * Sharing (CORS) policies.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class WebConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public WebConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures the security filter chain for the application.
     * <p>
     * This method sets up:
     * <ul>
     * <li>CORS configuration using a custom source.</li>
     * <li>Disables CSRF protection (common for stateless REST APIs).</li>
     * <li>Defines authorization rules:
     * <ul>
     * <li>Endpoints under {@code /api/public/**} are accessible without authentication.</li>
     * <li>Endpoints under {@code /api/auth/**} are accessible without authentication (Login/Refresh).</li>
     * <li>Actuator endpoints under {@code /actuator/**} are accessible without authentication (for demo purposes).</li>
     * <li>Swagger UI and API Docs endpoints are accessible without authentication.</li>
     * <li>H2 Console endpoints are accessible without authentication.</li>
     * <li>All other requests require authentication.</li>
     * </ul>
     * </li>
     * <li>Configures stateless session management.</li>
     * <li>Adds the JWT authentication filter before the UsernamePasswordAuthenticationFilter.</li>
     * <li>Disables X-Frame-Options to allow H2 Console to load in a frame.</li>
     * </ul>
     * </p>
     *
     * @param http
     *            the {@link HttpSecurity} to modify
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception
     *             if an error occurs while configuring security
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configure CORS using the defined bean
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Disable CSRF as we are using stateless REST API
                .csrf(AbstractHttpConfigurer::disable)
                // Define authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll() // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll() // Auth endpoints (Login, Refresh)
                        .requestMatchers("/actuator/**").permitAll() // Actuator endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger
                                                                                                              // endpoints
                        .requestMatchers("/h2-console/**").permitAll() // H2 Console
                        .anyRequest().authenticated() // Secure all other endpoints
                )
                // Configure Stateless Session Management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Allow frames for H2 Console
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                // Add JWT Filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures the authentication manager with a DAO authentication provider.
     * <p>
     * This uses the custom {@link UserDetailsService} and a password encoder to verify credentials against the
     * database.
     * </p>
     *
     * @param userDetailsService
     *            the custom user details service
     * @param passwordEncoder
     *            the password encoder
     * @return the configured {@link AuthenticationManager}
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

    /**
     * Configures the password encoder.
     * <p>
     * Uses BCrypt for secure password hashing.
     * </p>
     *
     * @return the {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     * <p>
     * This configuration allows requests from the specified frontend origin (e.g., Angular app running on
     * localhost:4200). It permits standard HTTP methods and headers, and allows credentials (cookies, auth headers).
     * </p>
     *
     * @return the {@link CorsConfigurationSource} with the defined rules
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow frontend origin
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        // Allow standard HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this configuration to all paths
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}