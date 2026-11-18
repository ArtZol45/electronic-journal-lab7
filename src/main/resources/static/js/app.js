const API = "/api/students";

let students = [];
let currentUser = null;

let userAccounts = [];

function isAdmin() {
    return currentUser?.role === "ROLE_ADMIN";
}

function isTeacher() {
    return currentUser?.role === "ROLE_TEACHER";
}

function isStudent() {
    return currentUser?.role === "ROLE_STUDENT";
}

function isAdminOrTeacher() {
    return isAdmin() || isTeacher();
}

// Проверка авторизации при открытии сайта
async function checkAuthentication() {
    try {
        const user = await apiRequest("/api/auth/me");
        await showAuthenticatedApp(user);
    } catch (error) {
        showLoginScreen();
    }
}

// Отображение страницы входа
function showLoginScreen() {
    document.getElementById("login-screen").classList.remove("hidden");
    document.getElementById("app").classList.add("hidden");
}

// Отображение журнала после успешного входа
async function showAuthenticatedApp(user) {
    currentUser = user;

    document.getElementById("login-screen").classList.add("hidden");
    document.getElementById("app").classList.remove("hidden");

    document.getElementById("current-username").textContent =
        user.username;

    document.getElementById("user-avatar").textContent =
        user.username.charAt(0).toUpperCase();

    const roles = {
        ROLE_ADMIN: "Администратор",
        ROLE_TEACHER: "Преподаватель",
        ROLE_STUDENT: "Студент"
    };

    document.getElementById("current-role").textContent =
        roles[user.role] || user.role;

    document.querySelectorAll(".admin-only").forEach(element => {
        element.classList.toggle("hidden", !isAdmin());
    });

    document.querySelectorAll(".admin-teacher-only").forEach(element => {
        element.classList.toggle("hidden", !isAdminOrTeacher());
    });

    if (isAdminOrTeacher()) {
        await loadStudents();
        showPage("dashboard");
        return;
    }

    if (isStudent()) {
        if (!user.studentId) {
            showMessage(
                "У этой учётной записи не указан студент. Обратитесь к администратору.",
                "error"
            );
            return;
        }

        students = [];

        // Заполняем фильтры только собственным ID.
        const studentId = String(user.studentId);

        ["grade-student-filter", "attendance-student-filter", "report-student"]
            .forEach(id => {
                const select = document.getElementById(id);
                select.innerHTML = `<option value="${studentId}">Мои данные</option>`;
                select.value = studentId;

                // Студенту не нужен выбор другого студента.
                if (id !== "report-student") {
                    select.closest("label")?.classList.add("hidden");
                } else {
                    select.classList.add("hidden");
                }
            });

        // Убираем кнопки выбора студента: данные загружаются автоматически.
        document.querySelectorAll(
            '#page-grades button[onclick="loadAllGrades()"], ' +
            '#page-attendance button[onclick="loadAllAttendance()"]'
        ).forEach(button => button.classList.add("hidden"));

        document.querySelector(
            '#page-reports button[onclick="loadReport()"]'
        ).textContent = "Сформировать мой отчёт";

        showPage("grades");
        await loadAllGrades();
    }
}

// Вход в систему
async function login(event) {
    event.preventDefault();

    const username = document.getElementById("login-username").value.trim();
    const password = document.getElementById("login-password").value;

    const errorElement = document.getElementById("login-error");
    const button = document.getElementById("login-button");

    errorElement.classList.add("hidden");
    button.disabled = true;
    button.textContent = "Выполняется вход...";

    try {
        const user = await apiRequest("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ username, password })
        });

        document.getElementById("login-password").value = "";

        await showAuthenticatedApp(user);
    } catch (error) {
        errorElement.textContent = "Неверный логин или пароль.";
        errorElement.classList.remove("hidden");
    } finally {
        button.disabled = false;
        button.textContent = "Войти";
    }
}

// Выход из системы
async function logout() {
    try {
        await apiRequest("/api/auth/logout", {
            method: "POST"
        });

        students = [];

        document.getElementById("login-form").reset();

        showLoginScreen();
    } catch (error) {
        showMessage("Не удалось выполнить выход: " + error.message, "error");
    }
}

const pageNames = {
    dashboard: "Главная",
    students: "Студенты",
    grades: "Оценки",
    attendance: "Посещаемость",
    schedule: "Расписание",
    reports: "Отчёты",
    users: "Пользователи"
};

document.addEventListener("DOMContentLoaded", async () => {
    document.getElementById("today").textContent =
        new Date().toLocaleDateString("ru-RU", {
            day: "numeric",
            month: "long",
            year: "numeric"
        });

    document.getElementById("schedule-date").value = today();
    document.getElementById("new-schedule-date").value = today();
    document.getElementById("new-attendance-date").value = today();

    document.querySelectorAll(".nav-item").forEach(button => {
        button.addEventListener("click", () => showPage(button.dataset.page));
    });

    document.getElementById("student-search").addEventListener("input", renderStudents);

    document.getElementById("login-form").addEventListener("submit", login);

    await checkAuthentication();
});

function today() {
    const date = new Date();
    const offset = date.getTimezoneOffset();
    return new Date(date.getTime() - offset * 60000).toISOString().slice(0, 10);
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;"
    })[character]);
}

async function apiRequest(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });

    const text = await response.text();

    if (!response.ok) {
        throw new Error(text || `Ошибка сервера: ${response.status}`);
    }

    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

function showMessage(message, type = "success") {
    const element = document.getElementById("message");
    element.textContent = message;
    element.className = `message ${type}`;

    window.scrollTo({ top: 0, behavior: "smooth" });

    setTimeout(() => element.classList.add("hidden"), 4500);
}

function showPage(page) {
        // Студенту запрещён доступ к общему списку студентов
    // и к разделу управления пользователями.
    if (isStudent() && (page === "students" || page === "users")) {
        page = "grades";
    }
    document.querySelectorAll(".page").forEach(element => {
        element.classList.remove("active");
    });

    document.querySelectorAll(".nav-item").forEach(element => {
        element.classList.toggle("active", element.dataset.page === page);
    });

    document.getElementById(`page-${page}`).classList.add("active");
    document.getElementById("page-title").textContent = pageNames[page];
    document.getElementById("breadcrumb").textContent = pageNames[page];

    if (page === "dashboard") {
        refreshDashboard();
    }

    if (page === "grades") {
        loadAllGrades();
    }

    if (page === "attendance") {
        loadAllAttendance();
    }

    if (page === "reports" && isStudent()) {
        loadReport();
    }

    if (page === "schedule") {
        loadSchedule();
    }
    if (page === "students") {
        loadStudents();
    }
    if (page === "users") {
        if (currentUser?.role === "ROLE_ADMIN") {
            loadUsers();
        }
    }
}

function openModal(id) {
    if (id === "grade-modal" || id === "attendance-modal") {
        if (students.length === 0) {
            showMessage("Сначала добавьте хотя бы одного студента.", "error");
            return;
        }
    }

    if (id === "grade-modal") {
        fillStudentSelect("new-grade-student");
    }

    if (id === "attendance-modal") {
        fillStudentSelect("new-attendance-student");
        document.getElementById("new-attendance-date").value = today();
    }

    if (id === "schedule-modal") {
        const hasGroups = fillScheduleGroupSelect();

        if (!hasGroups) {
            showMessage(
                "Сначала добавьте студента и укажите ему учебную группу.",
                "error"
            );
            return;
        }

        document.getElementById("new-schedule-date").value = today();
    }

    document.getElementById(id).classList.remove("hidden");
}

function closeModal(id) {
    document.getElementById(id).classList.add("hidden");
}

function fillStudentSelect(elementId) {
    const select = document.getElementById(elementId);

    select.innerHTML = students.map(student =>
        `<option value="${student.id}">${escapeHtml(student.fullName)} (ID: ${student.id})</option>`
    ).join("");
}


function fillScheduleGroupSelect() {
    const select = document.getElementById("new-schedule-group");

    const groups = [...new Set(
        students
            .map(student => student.groupName?.trim())
            .filter(group => group && group !== "Не указана")
    )].sort();

    select.innerHTML =
        '<option value="">Выберите группу</option>' +
        groups.map(group =>
            `<option value="${escapeHtml(group)}">${escapeHtml(group)}</option>`
        ).join("");

    return groups.length > 0;
}


function updateStudentSelectors() {
    [
        "grade-student-filter",
        "attendance-student-filter",
        "report-student"
    ].forEach(id => {
        const select = document.getElementById(id);
        const previousValue = select.value;

        select.innerHTML =
            '<option value="">Выберите студента</option>' +
            students.map(student =>
                `<option value="${student.id}">${escapeHtml(student.fullName)} (ID: ${student.id})</option>`
            ).join("");

        if (students.some(student => String(student.id) === previousValue)) {
            select.value = previousValue;
        }
    });
}

async function loadStudents() {
    try {
        students = await apiRequest(API);
        students.sort((a, b) => a.id - b.id);

        renderStudents();
        renderDashboardStudents();
        updateStudentSelectors();

        document.getElementById("student-count").textContent =
            `Всего: ${students.length}`;

        await refreshDashboard();
    } catch (error) {
        showMessage(`Не удалось загрузить студентов: ${error.message}`, "error");
    }
}

function renderStudents() {
    const tbody = document.getElementById("students-table");

    if (!students || students.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="empty">Студенты не найдены</td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = students.map(student => `
        <tr>
            <td>${student.id}</td>
            <td>${escapeHtml(student.fullName)}</td>
            <td>${escapeHtml(student.groupName || "Не указана")}</td>
            <td>
                <button class="btn btn-secondary"
                        onclick="openStudentReport(${student.id})">
                    Отчёт
                </button>
            </td>
            <td>
                ${isAdmin() ? `
                    <button class="btn btn-secondary"
                            onclick="openEditStudent(${student.id})">
                        Редактировать
                    </button>
                    <button class="btn btn-danger"
                            onclick="deactivateStudent(${student.id})">
                        Отключить
                    </button>
                ` : ""}
            </td>
        </tr>
    `).join("");
}

function renderDashboardStudents() {
    const tbody = document.getElementById("dashboard-students");
    const recent = students.slice(-5).reverse();

    if (recent.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="empty">Пока нет зарегистрированных студентов</td></tr>';
        return;
    }

    tbody.innerHTML = recent.map(student => `
        <tr>
            <td>${student.id}</td>
            <td>${escapeHtml(student.fullName)}</td>
            <td>
                <button class="button button-secondary"
                    onclick="openStudentReport(${student.id})">
                    Открыть отчёт
                </button>
            </td>
        </tr>
    `).join("");
}

async function refreshDashboard() {
    try {
        if (isStudent()) {
            document.getElementById("stat-students").textContent = "—";
            document.getElementById("stat-grades").textContent = "—";
            document.getElementById("stat-attendance").textContent = "—";
            document.getElementById("stat-average").textContent = "—";

            return;
        }
        const currentStudents = await apiRequest(API);
        students = currentStudents;
        students.sort((a, b) => a.id - b.id);

        renderDashboardStudents();
        renderStudents();
        updateStudentSelectors();

        let totalGrades = 0;
        let totalAttendance = 0;
        let gradeSum = 0;

        const results = await Promise.all(
            students.map(async student => {
                try {
                    const [grades, attendance] = await Promise.all([
                        apiRequest(`${API}/${student.id}/grades`),
                        apiRequest(`${API}/${student.id}/attendance`)
                    ]);

                    return { grades, attendance };
                } catch {
                    return { grades: [], attendance: [] };
                }
            })
        );

        results.forEach(result => {
            totalGrades += result.grades.length;
            totalAttendance += result.attendance.length;

            result.grades.forEach(grade => {
                gradeSum += Number(grade.value);
            });
        });

        document.getElementById("stat-students").textContent = students.length;
        document.getElementById("stat-grades").textContent = totalGrades;
        document.getElementById("stat-attendance").textContent = totalAttendance;
        document.getElementById("stat-average").textContent =
            totalGrades ? (gradeSum / totalGrades).toFixed(2) : "—";
    } catch (error) {
        showMessage(`Не удалось обновить главную страницу: ${error.message}`, "error");
    }
}

async function addStudent(event) {
    event.preventDefault();

    const id = Number(
        document.getElementById("new-student-id").value
    );

    const fullName =
        document.getElementById("new-student-name").value.trim();

    const groupName =
        document.getElementById("new-student-group").value.trim();

    const username =
        document.getElementById("new-student-username").value.trim();

    const password =
        document.getElementById("new-student-password").value;

    if (!fullName || !groupName || !username || !password) {
        showMessage("Заполните все поля.", "error");
        return;
    }

    try {
        // Создаём студента вместе с учебной группой.
        await apiRequest(API, {
            method: "POST",
            body: JSON.stringify({
                id,
                fullName,
                groupName
            })
        });

        // Создаём учётную запись и связываем её со студентом.
        await apiRequest("/api/admin/users", {
            method: "POST",
            body: JSON.stringify({
                username,
                password,
                role: "STUDENT",
                studentId: id
            })
        });

        closeModal("student-modal");
        event.target.reset();

        await loadStudents();
        await loadUsers();

        showMessage("Студент, его группа и учётная запись успешно созданы.");
    } catch (error) {
        showMessage(
            "Не удалось полностью создать студента. " +
            error.message +
            " Проверьте списки студентов и пользователей перед повторной попыткой.",
            "error"
        );
    }
}

async function addTeacher(event) {
    event.preventDefault();

    const username =
        document.getElementById("new-teacher-username").value.trim();

    const password =
        document.getElementById("new-teacher-password").value;

    try {
        await apiRequest("/api/admin/users", {
            method: "POST",
            body: JSON.stringify({
                username,
                password,
                role: "TEACHER"
            })
        });

        closeModal("teacher-modal");
        event.target.reset();

        await loadUsers();

        showMessage("Учётная запись преподавателя успешно создана.");
    } catch (error) {
        showMessage(
            `Не удалось создать преподавателя: ${error.message}`,
            "error"
        );
    }
}


async function loadUsers() {
    const tbody = document.getElementById("users-table");

    try {
        const users = await apiRequest("/api/admin/users");

        // Сохраняем пользователей для последующего редактирования
        userAccounts = users;

        const studentsCount = users.filter(
            user => user.role === "STUDENT"
        ).length;

        const teachersCount = users.filter(
            user => user.role === "TEACHER"
        ).length;

        document.getElementById("users-total").textContent =
            users.length;

        document.getElementById("users-students").textContent =
            studentsCount;

        document.getElementById("users-teachers").textContent =
            teachersCount;

        if (users.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="empty">
                        Пользователей пока нет
                    </td>
                </tr>
            `;
            return;
        }

        const roleNames = {
            ADMIN: "Администратор",
            TEACHER: "Преподаватель",
            STUDENT: "Студент"
        };

        tbody.innerHTML = users.map(user => {
            const active = user.active !== false;

            let actions = "—";

            
        if (user.role === "TEACHER" && active) {
            actions = `
                <button class="btn btn-secondary"
                        onclick="openEditTeacher(${user.id})">
                    Редактировать
                </button>
                <button class="btn btn-danger"
                        onclick="deactivateTeacher(${user.id})">
                    Отключить
                </button>
            `;
        } else if (user.role === "STUDENT" && active && user.studentId) {
            actions = `
                <button class="btn btn-secondary"
                        onclick="openEditStudent(${user.studentId})">
                    Редактировать
                </button>
                <button class="btn btn-danger"
                        onclick="deactivateStudent(${user.studentId})">
                    Отключить
                </button>
            `;
        } else if (!active) {
            actions = "Учётная запись отключена";
        }


            return `
                <tr>
                    <td>${user.id}</td>
                    <td><strong>${escapeHtml(user.username)}</strong></td>
                    <td>${roleNames[user.role] || escapeHtml(user.role)}</td>
                    <td>${user.studentId ?? "—"}</td>
                    <td>
                        ${active ? "Активен" : "Отключён"}
                        <br>
                        ${actions}
                    </td>
                </tr>
            `;
        }).join("");

    } catch (error) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="empty">
                    Не удалось загрузить пользователей
                </td>
            </tr>
        `;

        showMessage(
            `Ошибка загрузки пользователей: ${error.message}`,
            "error"
        );
    }
}

function openEditTeacher(userId) {
    const user = userAccounts.find(item => item.id === userId);

    if (!user || user.role !== "TEACHER" || user.active === false) {
        showMessage("Преподаватель не найден или отключён", "error");
        return;
    }

    document.getElementById("edit-teacher-id").value = user.id;
    document.getElementById("edit-teacher-username").value = user.username;
    document.getElementById("edit-teacher-password").value = "";

    document.getElementById("edit-teacher-modal").classList.remove("hidden");
}

async function updateTeacher(event) {
    event.preventDefault();

    const id = document.getElementById("edit-teacher-id").value;
    const username = document.getElementById("edit-teacher-username").value.trim();
    const password = document.getElementById("edit-teacher-password").value;

    if (!username) {
        showMessage("Введите логин преподавателя", "error");
        return;
    }

    const requestBody = { username };

    if (password.trim()) {
        requestBody.password = password;
    }

    try {
        await apiRequest(`/api/admin/users/${id}`, {
            method: "PUT",
            body: JSON.stringify(requestBody)
        });

        document.getElementById("edit-teacher-modal").classList.add("hidden");

        await loadUsers();

        showMessage("Данные преподавателя обновлены", "success");
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function deactivateTeacher(userId) {
    const user = userAccounts.find(item => item.id === userId);

    if (!user || user.role !== "TEACHER") {
        showMessage("Преподаватель не найден", "error");
        return;
    }

    if (!confirm(`Отключить учётную запись преподавателя ${user.username}?`)) {
        return;
    }

    try {
        await apiRequest(`/api/admin/users/${userId}`, {
            method: "DELETE"
        });

        await loadUsers();

        showMessage("Учётная запись преподавателя отключена", "success");
    } catch (error) {
        showMessage(error.message, "error");
    }
}


async function loadAllGrades() {
    const studentId = isStudent()
        ? currentUser.studentId
        : document.getElementById("grade-student-filter").value;

    const tbody = document.getElementById("grades-table");

    if (!studentId) {
        tbody.innerHTML = '<tr><td colspan="3" class="empty">Сначала выберите студента</td></tr>';
        return;
    }

    try {
        const grades = await apiRequest(`${API}/${studentId}/grades`);

        if (grades.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="empty">Оценок пока нет</td></tr>';
            return;
        }

        tbody.innerHTML = grades.map(grade => `
            <tr>
                <td>${grade.studentId}</td>
                <td>${escapeHtml(grade.subject)}</td>
                <td><span class="grade-badge">${grade.value}</span></td>
            </tr>
        `).join("");
    } catch (error) {
        showMessage(`Не удалось загрузить оценки: ${error.message}`, "error");
    }
}

async function addGrade(event) {
    event.preventDefault();

    const studentId = document.getElementById("new-grade-student").value;
    const subject = document.getElementById("new-grade-subject").value.trim();
    const value = Number(document.getElementById("new-grade-value").value);

    try {
        await apiRequest(`${API}/${studentId}/grades`, {
            method: "POST",
            body: JSON.stringify({ subject, value })
        });

        closeModal("grade-modal");
        event.target.reset();

        document.getElementById("grade-student-filter").value = studentId;
        await loadAllGrades();
        await refreshDashboard();

        showMessage("Оценка успешно сохранена.");
    } catch (error) {
        showMessage(`Не удалось сохранить оценку: ${error.message}`, "error");
    }
}

async function loadAllAttendance() {
    const studentId = isStudent()
        ? currentUser.studentId
        : document.getElementById("attendance-student-filter").value;

    const tbody = document.getElementById("attendance-table");

    if (!studentId) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty">Сначала выберите студента</td></tr>';
        return;
    }

    try {
        const records = await apiRequest(`${API}/${studentId}/attendance`);

        if (records.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="empty">Записей о посещаемости пока нет</td></tr>';
            return;
        }

        tbody.innerHTML = records.map(record => `
            <tr>
                <td>${escapeHtml(record.date)}</td>
                <td>${record.studentId}</td>
                <td>${escapeHtml(record.subject)}</td>
                <td>
                    <span class="badge ${record.present ? "badge-success" : "badge-danger"}">
                        ${record.present ? "Присутствовал" : "Отсутствовал"}
                    </span>
                </td>
            </tr>
        `).join("");
    } catch (error) {
        showMessage(`Не удалось загрузить посещаемость: ${error.message}`, "error");
    }
}

async function addAttendance(event) {
    event.preventDefault();

    const studentId = document.getElementById("new-attendance-student").value;
    const date = document.getElementById("new-attendance-date").value;
    const subject = document.getElementById("new-attendance-subject").value.trim();
    const present = document.getElementById("new-attendance-present").value === "true";

    try {
        await apiRequest(`${API}/${studentId}/attendance`, {
            method: "POST",
            body: JSON.stringify({ date, subject, present })
        });

        closeModal("attendance-modal");
        event.target.reset();

        document.getElementById("attendance-student-filter").value = studentId;
        await loadAllAttendance();
        await refreshDashboard();

        showMessage("Отметка посещаемости успешно сохранена.");
    } catch (error) {
        showMessage(`Не удалось сохранить посещаемость: ${error.message}`, "error");
    }
}


async function loadSchedule() {
    const date = document.getElementById("schedule-date").value;
    const tbody = document.getElementById("schedule-table");

    if (!date) {
        showMessage("Выберите дату занятий.", "error");
        return;
    }

    try {
        const schedule = await apiRequest(
            `${API}/schedule?date=${encodeURIComponent(date)}`
        );

        if (schedule.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="empty">
                        На выбранную дату занятий нет
                    </td>
                </tr>
            `;
            return;
        }

        schedule.sort((a, b) =>
            String(a.time).localeCompare(String(b.time))
        );

        tbody.innerHTML = schedule.map(entry => `
            <tr>
                <td>
                    <strong>${escapeHtml(String(entry.time).slice(0, 5))}</strong>
                </td>
                <td>${escapeHtml(entry.subject)}</td>
                <td>${escapeHtml(entry.groupName || "Не указана")}</td>
                <td>${escapeHtml(entry.classroom)}</td>
                <td>${escapeHtml(entry.date)}</td>
            </tr>
        `).join("");
    } catch (error) {
        showMessage(
            `Не удалось загрузить расписание: ${error.message}`,
            "error"
        );
    }
}



async function addSchedule(event) {
    event.preventDefault();

    const date = document.getElementById("new-schedule-date").value;
    const time = document.getElementById("new-schedule-time").value;
    const subject = document.getElementById("new-schedule-subject").value.trim();
    const classroom = document.getElementById("new-schedule-classroom").value.trim();
    const groupName = document.getElementById("new-schedule-group").value;

    if (!groupName) {
        showMessage("Выберите учебную группу.", "error");
        return;
    }

    try {
        await apiRequest(`${API}/schedule`, {
            method: "POST",
            body: JSON.stringify({
                date,
                time,
                subject,
                classroom,
                groupName
            })
        });

        closeModal("schedule-modal");
        event.target.reset();

        document.getElementById("schedule-date").value = date;
        await loadSchedule();

        showMessage("Занятие успешно добавлено в расписание.");
    } catch (error) {
        showMessage(`Не удалось добавить занятие: ${error.message}`, "error");
    }
}


async function loadReport() {
    const studentId = isStudent()
        ? currentUser.studentId
        : document.getElementById("report-student").value;

    const result = document.getElementById("report-result");

    if (!studentId) {
        showMessage("Выберите студента для формирования отчёта.", "error");
        return;
    }

    try {
        const report = await apiRequest(`${API}/${studentId}/report`);

        result.innerHTML = `
            <h3>Отчёт по студенту №${report.studentId}</h3>
            <p><strong>${escapeHtml(report.fullName)}</strong></p>
            <div class="report-grid">
                <div class="report-item">
                    <small>Количество оценок</small>
                    <strong>${report.gradesCount}</strong>
                </div>
                <div class="report-item">
                    <small>Средний балл</small>
                    <strong>${Number(report.averageGrade).toFixed(2)}</strong>
                </div>
                <div class="report-item">
                    <small>Всего отметок</small>
                    <strong>${report.attendanceCount}</strong>
                </div>
                <div class="report-item">
                    <small>Присутствовал</small>
                    <strong>${report.attendedCount}</strong>
                </div>
                <div class="report-item">
                    <small>Отсутствовал</small>
                    <strong>${report.absencesCount}</strong>
                </div>
                <div class="report-item">
                    <small>Посещаемость</small>
                    <strong>${report.attendanceCount
                        ? (report.attendedCount / report.attendanceCount * 100).toFixed(1) + "%"
                        : "—"}</strong>
                </div>
            </div>
            <button class="button button-secondary" onclick="window.print()" style="margin-top:18px">
                Распечатать отчёт
            </button>
        `;
    } catch (error) {
        showMessage(`Не удалось сформировать отчёт: ${error.message}`, "error");
    }
}

function openStudentReport(studentId) {
    showPage("reports");
    document.getElementById("report-student").value = String(studentId);
    loadReport();
}

function openEditStudent(studentId) {
    const student = students.find(
        item => item.id === studentId
    );

    if (!student) {
        showMessage("Студент не найден", "error");
        return;
    }

    document.getElementById("edit-student-id").value = student.id;

    document.getElementById("edit-student-name").value =
        student.fullName;

    document.getElementById("edit-student-group").value =
        student.groupName || "";

    document.getElementById("edit-student-modal").classList.remove("hidden");
}

async function updateStudent(event) {
    event.preventDefault();

    const id = document.getElementById("edit-student-id").value;

    const fullName =
        document.getElementById("edit-student-name").value.trim();

    const groupName =
        document.getElementById("edit-student-group").value.trim();

    if (!fullName) {
        showMessage("Введите ФИО студента", "error");
        return;
    }

    if (!groupName) {
        showMessage("Введите учебную группу", "error");
        return;
    }

    try {
        await apiRequest(`${API}/${id}`, {
            method: "PUT",
            body: JSON.stringify({
                fullName,
                groupName
            })
        });

        document.getElementById("edit-student-modal").classList.add("hidden");

        await loadStudents();

        showMessage("Данные студента обновлены", "success");
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function deactivateStudent(studentId) {
    if (!confirm("Отключить этого студента? Он больше не сможет войти в систему.")) {
        return;
    }

    try {
        await apiRequest(`${API}/${studentId}`, {
            method: "DELETE"
        });

        await loadStudents();
        await loadUsers();

        showMessage("Студент отключён", "success");
    } catch (error) {
        showMessage(error.message, "error");
    }
}