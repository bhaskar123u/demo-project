package com.bsharan.demo_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http){
        http.authorizeHttpRequests((request)->
            request.requestMatchers("/register").permitAll()
                    .requestMatchers("/users").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        // Allow only 1 active session per user, and if a second login happens, reject it instead of kicking out the first session.
        .sessionManagement(session ->
                session.maximumSessions(1)
                       .maxSessionsPreventsLogin(true)
        )
        .csrf(csrf -> csrf.disable())
        .formLogin(withDefaults()) // here we can add our own custom login page
        .httpBasic(withDefaults());
        return http.build();
    }
}
