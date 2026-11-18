package ru.university.journal;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserAccountRepository userAccountRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()
                        )
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );

        Map<String, Object> result = getUserData(authentication);
        result.put("message", "Вход выполнен успешно");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    public Map<String, Object> currentUser(
            Authentication authentication
    ) {
        return getUserData(authentication);
    }

    private Map<String, Object> getUserData(
            Authentication authentication
    ) {
        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("username", authentication.getName());
        result.put("role", role);

        if ("ROLE_STUDENT".equals(role)) {
            UserAccount account = userAccountRepository
                    .findByUsername(authentication.getName())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Учётная запись не найдена"
                            )
                    );

            if (account.getStudent() == null) {
                throw new IllegalStateException(
                        "Для учётной записи студента не указан студент"
                );
            }

            result.put("studentId", account.getStudent().getId());
        }

        return result;
    }

    @PostMapping("/logout")
    public Map<String, String> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        new SecurityContextLogoutHandler().logout(
                request,
                response,
                authentication
        );

        return Map.of("message", "Выход выполнен");
    }

    public record LoginRequest(
            String username,
            String password
    ) {
    }
}
