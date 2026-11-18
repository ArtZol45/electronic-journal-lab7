
package ru.university.journal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public StudentService(
            StudentRepository studentRepository,
            GradeRepository gradeRepository,
            AttendanceRepository attendanceRepository,
            ScheduleRepository scheduleRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
        this.attendanceRepository = attendanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.userAccountRepository = userAccountRepository;
    }
        // Конструктор для существующих unit-тестов
    public StudentService(
            StudentRepository studentRepository,
            GradeRepository gradeRepository,
            AttendanceRepository attendanceRepository,
            ScheduleRepository scheduleRepository
    ) {
        this(
                studentRepository,
                gradeRepository,
                attendanceRepository,
                scheduleRepository,
                null
        );
    }

    // Добавление студента
    public Student addStudent(long id, String fullName, String groupName) {
        if (studentRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Студент с таким ID уже существует"
            );
        }

        Student student = new Student(id, fullName, groupName);

        return studentRepository.save(student);
    }

    // Старый метод для совместимости с существующими тестами
    public Student addStudent(long id, String fullName) {
        return addStudent(id, fullName, "Не указана");
    }


    // Получение только активных студентов
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findByActiveTrueOrderByIdAsc();
    }

    
    // Редактирование студента
    public Student updateStudent(
            long studentId,
            String fullName,
            String groupName
    ) {
        Student student = findActiveStudent(studentId);

        student.setFullName(fullName);
        student.setGroupName(groupName);

        return student;
    }

    // Старый метод для совместимости с существующими тестами
    public Student updateStudent(long studentId, String fullName) {
        Student student = findActiveStudent(studentId);

        student.setFullName(fullName);

        return student;
    }


    // Мягкое удаление студента
    public Student deactivateStudent(long studentId) {
        Student student = findActiveStudent(studentId);
        student.setActive(false);

        if (userAccountRepository != null) {
                userAccountRepository.findByStudent_Id(studentId)
                        .ifPresent(account -> account.setActive(false));
        }

        return student;
    }

    // Добавление оценки
    public Grade addGrade(
            long studentId,
            String subject,
            int value
    ) {
        Student student = findActiveStudent(studentId);

        Grade grade = new Grade(student, subject, value);

        return gradeRepository.save(grade);
    }

    // Получение оценок студента
    @Transactional(readOnly = true)
    public List<Grade> getGradesByStudent(long studentId) {
        findStudent(studentId);

        return gradeRepository
                .findByStudent_IdOrderByIdAsc(studentId);
    }

    // Добавление посещаемости
    public Attendance addAttendance(
            long studentId,
            LocalDate date,
            String subject,
            boolean present
    ) {
        Student student = findActiveStudent(studentId);

        Attendance attendance = new Attendance(
                student,
                date,
                subject,
                present
        );

        return attendanceRepository.save(attendance);
    }

    // Получение посещаемости студента
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByStudent(
            long studentId
    ) {
        findStudent(studentId);

        return attendanceRepository
                .findByStudent_IdOrderByDateAsc(studentId);
    }

    
    // Добавление занятия в расписание
    public ScheduleEntry addScheduleEntry(
            LocalDate date,
            LocalTime time,
            String subject,
            String classroom,
            String groupName
    ) {
        ScheduleEntry entry = new ScheduleEntry(
                date,
                time,
                subject,
                classroom,
                groupName
        );

        return scheduleRepository.save(entry);
    }

    // Старый метод для совместимости с существующими тестами
    public ScheduleEntry addScheduleEntry(
            LocalDate date,
            LocalTime time,
            String subject,
            String classroom
    ) {
        return addScheduleEntry(
                date,
                time,
                subject,
                classroom,
                "Не указана"
        );
    }


    // Получение расписания на дату
    @Transactional(readOnly = true)
    public List<ScheduleEntry> getScheduleByDate(
            LocalDate date
    ) {
        if (date == null) {
            throw new IllegalArgumentException(
                    "Дата не должна быть пустой"
            );
        }

        return scheduleRepository
                .findByDateOrderByTimeAsc(date);
    }

    // Формирование отчёта по студенту
    @Transactional(readOnly = true)
    public StudentReport getStudentReport(long studentId) {
        Student student = findStudent(studentId);

        List<Grade> studentGrades =
                gradeRepository
                        .findByStudent_IdOrderByIdAsc(studentId);

        List<Attendance> studentAttendance =
                attendanceRepository
                        .findByStudent_IdOrderByDateAsc(studentId);

        double averageGrade = studentGrades.stream()
                .mapToInt(Grade::getValue)
                .average()
                .orElse(0.0);

        int attendedCount = (int) studentAttendance.stream()
                .filter(Attendance::isPresent)
                .count();

        int absencesCount =
                studentAttendance.size() - attendedCount;

        return new StudentReport(
                student.getId(),
                student.getFullName(),
                studentGrades.size(),
                averageGrade,
                studentAttendance.size(),
                attendedCount,
                absencesCount
        );
    }

    // Поиск студента, включая неактивных
    private Student findStudent(long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Студент с таким ID не найден"
                ));
    }

    // Поиск только активного студента
    private Student findActiveStudent(long studentId) {
        Student student = findStudent(studentId);

        if (!student.isActive()) {
            throw new IllegalArgumentException(
                    "Студент неактивен"
            );
        }

        return student;
    }
}