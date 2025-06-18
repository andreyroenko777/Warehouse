package com.wms.warehouse.config;

import com.wms.warehouse.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/write-off/**").hasAnyRole("ADMIN", "WAREHOUSE", "SUPER_ADMIN")
                        .requestMatchers("/products/new").hasAnyRole("ADMIN", "MANAGER", "SUPER_ADMIN", "WAREHOUSE")
                        .requestMatchers("/reports/**").hasAnyRole("ADMIN", "MANAGER", "SUPER_ADMIN")
                        .requestMatchers("/products").hasAnyRole("MANAGER", "ADMIN", "SUPER_ADMIN", "WAREHOUSE")
                        .requestMatchers("/batches/**").hasAnyRole("ADMIN", "WAREHOUSE", "SUPER_ADMIN")
                        .requestMatchers("/stock").hasAnyRole("ADMIN", "MANAGER", "WAREHOUSE", "SUPER_ADMIN")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form

                        .loginPage("/login") // кастомная страница логина
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
