package ru.university.journal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public AdminAccountInitializer(
            UserAccountRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (adminPassword == null || adminPassword.isBlank()) {
            LOGGER.warn(
                    "Первый администратор не создан: "
                            + "не задана переменная ADMIN_PASSWORD"
            );
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            LOGGER.info("Учётная запись администратора уже существует");
            return;
        }

        UserAccount admin = new UserAccount(
                adminUsername,
                passwordEncoder.encode(adminPassword),
                UserRole.ADMIN,
                null
        );

        userRepository.save(admin);

        LOGGER.info(
                "Создана учётная запись администратора: {}",
                adminUsername
        );
    }
}