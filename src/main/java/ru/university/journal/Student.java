package ru.university.journal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {

    @Id
    private long id;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "group_name", nullable = false, length = 50)
    private String groupName = "Не указана";

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    protected Student() {
    }

    // Старый конструктор для совместимости с существующим кодом и тестами
    public Student(long id, String fullName) {
        this(id, fullName, "Не указана");
    }

    // Новый конструктор с указанием учебной группы
    public Student(long id, String fullName, String groupName) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "ID студента должен быть положительным"
            );
        }

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "ФИО студента не должно быть пустым"
            );
        }

        this.id = id;
        this.fullName = fullName.trim();
        setGroupName(groupName);
        this.active = true;
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isActive() {
        return active;
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "ФИО студента не должно быть пустым"
            );
        }

        this.fullName = fullName.trim();
    }

    public void setGroupName(String groupName) {
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException(
                    "Название группы не должно быть пустым"
            );
        }

        this.groupName = groupName.trim();
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}