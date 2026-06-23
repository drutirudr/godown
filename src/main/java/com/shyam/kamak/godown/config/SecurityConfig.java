package com.shyam.kamak.godown.config;

import com.shyam.kamak.godown.security.JwtAuthenticationFilter;
import com.shyam.kamak.godown.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;


import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // 1. Explicitly enable CORS and link your configuration source
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//
//                // 2. Your other configurations (CSRF, Authorization, etc.)
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/auth/**").permitAll() // Explictly open up the register API
//                        .requestMatchers("/api/public/**").permitAll()
//                        .requestMatchers("/error").permitAll()
////                         .requestMatchers("/v3/api-docs/**",
////                                 "/v3/api-docs/**",
////                                 "/v3/api-docs.yaml",
////                                 "/swagger-ui/**",
////                                 "/swagger-ui.html",
////                                 "/swagger-resources/**",
////                                 "/webjars/**"
////                         ).permitAll()
//                        .anyRequest().authenticated()
//                )
//                //.formLogin(Customizer.withDefaults())
//                //.httpBasic(Customizer.withDefaults());
//                // Add your custom JWT filter here:
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//
//        return http.build();
//    }

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            // 1. Explicitly enable CORS and link your configuration source
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 2. Your other configurations (CSRF, Authorization, etc.)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/auth/**").permitAll() // Explictly open up the register API
                            .requestMatchers("/api/public/**").permitAll()
                            .requestMatchers("/error").permitAll()
//                         .requestMatchers("/v3/api-docs/**",
//                                 "/v3/api-docs/**",
//                                 "/v3/api-docs.yaml",
//                                 "/swagger-ui/**",
//                                 "/swagger-ui.html",
//                                 "/swagger-resources/**",
//                                 "/webjars/**"
//                         ).permitAll()
                            .anyRequest().permitAll()
            )
            //.formLogin(Customizer.withDefaults())
            //.httpBasic(Customizer.withDefaults());
            // Add your custom JWT filter here:
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


    return http.build();
}

    // 2. Define the CorsConfigurationSource Bean
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Match the exact origin of your frontend (No trailing slash!)
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // Explicitly allow standard HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow necessary headers (Authorization, Content-Type, etc.)
        configuration.setAllowedHeaders(List.of("*"));

        // Set to true if your frontend sends cookies, auth tokens, or sessions
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply to all backend paths
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        //return new BCryptPasswordEncoder(); // Matches the hash format stored in the database
        return NoOpPasswordEncoder.getInstance(); // ⚠️ Tells Spring Security to compare passwords as raw, unencrypted strings
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        // Production-grade standard hashing variable
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
//        // Enforcing credential isolation for factory operators
//        UserDetails clerk = User.builder()
//                .username("clerk")
//                .password(passwordEncoder.encode("ClerkPass123!"))
//                .roles("BILLING_CLERK")
//                .build();
//
//        UserDetails admin = User.builder()
//                .username("admin")
//                .password(passwordEncoder.encode("AdminSuperPass99#"))
//                .roles("ADMIN", "BILLING_CLERK")
//                .build();
//
//        return new InMemoryUserDetailsManager(clerk, admin);
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                // Disabling CSRF since REST APIs use stateless session execution blocks
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        // Fabric Configuration Restrictions
//                        .requestMatchers(HttpMethod.POST, "/api/fabrics/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/fabrics/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/fabrics/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.GET, "/api/fabrics/**").hasAnyRole("BILLING_CLERK", "ADMIN")
//
//                        // Bundles & Invoicing Access Rules
//                        .requestMatchers("/api/bundles/**").hasAnyRole("BILLING_CLERK", "ADMIN")
//                        .requestMatchers("/api/bills/**").hasAnyRole("BILLING_CLERK", "ADMIN")
//
//                        // Block everything else by default
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(Customizer.withDefaults()); // Utilises standard Basic Auth headers for simple integration
//
//        return http.build();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // Maps directly to your local React dev server
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
//        configuration.setExposedHeaders(List.of("Authorization"));
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
}

