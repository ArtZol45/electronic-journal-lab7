package ru.university.journal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .securityContext(context -> context
                        .securityContextRepository(
                                securityContextRepository
                        )
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .authorizeHttpRequests(auth -> auth

                    .requestMatchers(
                            "/",
                            "/index.html",
                            "/css/**",
                            "/js/**",
                            "/api/auth/login",
                            "/swagger-ui/**",
                            "/v3/api-docs/**"
                    ).permitAll()

                    .requestMatchers("/api/admin/users/**")
                    .hasRole("ADMIN")

                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/students",
                            "/api/students/*/grades",
                            "/api/students/*/attendance",
                            "/api/students/schedule"
                    ).hasAnyRole("ADMIN", "TEACHER")

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/students/schedule"
                    ).hasAnyRole("ADMIN", "TEACHER", "STUDENT")

                    .requestMatchers("/api/students/**")
                    .authenticated()

                    .requestMatchers("/api/**")
                    .authenticated()

                    .anyRequest()
                    .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}