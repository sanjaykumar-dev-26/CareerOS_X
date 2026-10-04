package com.careerosx.backend.config;


import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.careerosx.backend.security.JwtAuthenticationFilter;

@Configuration 
public class SecurityConfig {

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    
    http.csrf(csrf -> csrf.disable());
    http.addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
    );
    http.authorizeHttpRequests(auth ->
        auth.requestMatchers("/auth/**").permitAll()
            .anyRequest().authenticated()
    );
    http.exceptionHandling(exception ->
    exception.authenticationEntryPoint(
        (request, response, authException) ->
            response.setStatus(401)
        )
    );
    return http.build();
    }
}
