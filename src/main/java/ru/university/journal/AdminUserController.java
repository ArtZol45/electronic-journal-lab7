
package ru.university.journal;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserAccountRepository userAccountRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(
            UserAccountRepository userAccountRepository,
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userAccountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @Transactional
    public UserResponse createUser(
            @RequestBody CreateUserRequest request
    ) {
        if (request.username() == null
                || request.username().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Укажите логин"
            );
        }

        if (request.password() == null
                || request.password().length() < 6) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Пароль должен содержать не менее 6 символов"
            );
        }

        if (request.role() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Укажите роль пользователя"
            );
        }

        UserRole role;

        try {
            role = UserRole.valueOf(
                    request.role().trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Неизвестная роль"
            );
        }

        if (role == UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Создание администратора через этот метод запрещено"
            );
        }

        String username = request.username().trim();

        if (userAccountRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким логином уже существует"
            );
        }

        Student student = null;

        if (role == UserRole.STUDENT) {
            if (request.studentId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Для студента необходимо указать ID студента"
                );
            }

            student = studentRepository.findById(
                    request.studentId()
            ).orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Студент с таким ID не найден"
                    )
            );

            if (!student.isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Нельзя создать учётную запись неактивному студенту"
                );
            }

            if (userAccountRepository.existsByStudent_Id(student.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Для этого студента уже создана учётная запись"
                );
            }
        }

        UserAccount account = new UserAccount(
                username,
                passwordEncoder.encode(request.password()),
                role,
                student
        );

        return toResponse(userAccountRepository.save(account));
    }

    @PutMapping("/{id}")
    @Transactional
    public UserResponse updateTeacher(
            @PathVariable Long id,
            @RequestBody UpdateTeacherRequest request
    ) {
        UserAccount account = userAccountRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Пользователь не найден"
                        )
                );

        if (account.getRole() != UserRole.TEACHER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Можно редактировать только учётные записи преподавателей"
            );
        }

        if (!account.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Нельзя редактировать отключённую учётную запись"
            );
        }

        if (request.username() == null
                || request.username().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Укажите логин"
            );
        }

        String username = request.username().trim();

        if (userAccountRepository.existsByUsernameAndIdNot(
                username,
                id
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким логином уже существует"
            );
        }

        account.setUsername(username);

        if (request.password() != null
                && !request.password().isBlank()) {

            if (request.password().length() < 6) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Новый пароль должен содержать не менее 6 символов"
                );
            }

            account.setPasswordHash(
                    passwordEncoder.encode(request.password())
            );
        }

        return toResponse(account);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public UserResponse deleteTeacher(
            @PathVariable Long id
    ) {
        UserAccount account = userAccountRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Пользователь не найден"
                        )
                );

        if (account.getRole() != UserRole.TEACHER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Этим методом можно отключить только преподавателя"
            );
        }

        account.setActive(false);

        return toResponse(account);
    }

    private UserResponse toResponse(UserAccount account) {
        return new UserResponse(
                account.getId(),
                account.getUsername(),
                account.getRole().name(),
                account.getStudent() == null
                        ? null
                        : account.getStudent().getId(),
                account.isActive()
        );
    }

    public record CreateUserRequest(
            String username,
            String password,
            String role,
            Long studentId
    ) {
    }

    public record UpdateTeacherRequest(
            String username,
            String password
    ) {
    }

    public record UserResponse(
            Long id,
            String username,
            String role,
            Long studentId,
            boolean active
    ) {
    }
}