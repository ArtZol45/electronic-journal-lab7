package ru.university.journal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("studentAccessService")
public class StudentAccessService {

    private final UserAccountRepository userAccountRepository;

    public StudentAccessService(
            UserAccountRepository userAccountRepository
    ) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public boolean canAccessStudent(
            String username,
            long studentId
    ) {
        return userAccountRepository.findByUsername(username)
                .map(account ->
                        account.getRole() == UserRole.STUDENT
                                && account.getStudent() != null
                                && account.getStudent().getId() == studentId
                )
                .orElse(false);
    }
}
