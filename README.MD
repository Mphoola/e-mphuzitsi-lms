# 🎓 Secondary School LMS (Spring Boot)

A **Learning Management System (LMS)** built with **Spring Boot**, designed for **secondary school students**.
It allows teachers to manage lessons, quizzes, discussions, and subjects while students can register for subjects, attempt quizzes, and participate in discussions.
Includes a **role & permission system** similar to **Laravel Spatie** for fine-grained access control.

---

## 🚀 Features

* 👩‍🏫 **User Roles & Permissions** (Student, Teacher, Admin + fine-grained permissions)
* 📚 **Subjects & Lessons** (video, PDF, quizzes)
* 📝 **Quizzes & Attempts** (with scoring and tracking by academic year)
* 💬 **Discussions** (tied to subjects & academic years)
* 📅 **Academic Year Tracking** (all content and interactions scoped per year)
* 🔒 **Secure Authentication & Authorization** with Spring Security
* 🛠️ **RESTful API** (strict MVC separation, backend-only)

---

## 🛠️ Tech Stack

* **Backend:** Spring Boot 3+, Spring Security, Spring Data JPA, Hibernate
* **Database:** MySQL 8+
* **Auth:** JWT (JSON Web Tokens)
* **Build Tool:** Maven or Gradle (depending on setup)
* **Other:** Lombok, HikariCP, Flyway (for DB migrations, optional)

---

## 📂 Project Structure (Package-by-Layer)

```
src/main/java/com/example/lms
│── config/          # Security, JWT, Web configs
│── controllers/     # REST Controllers
│── dto/             # Data Transfer Objects
│── entities/        # JPA Entities (with Lombok)
│── repositories/    # Spring Data Repositories
│── services/        # Business Logic Services
│── exceptions/      # Custom Exception Handlers
│── utils/           # Helper classes
```

---

## 🗄️ Database Design

See full design 👉 [db.md](./db.md)

Main Entities:

* Users, Roles, Permissions, UserRoles, RolePermissions, UserPermissions
* AcademicYears, Subjects, StudentSubjects
* LessonComponents, CourseContents
* Quizzes, QuizQuestions, QuizOptions, QuizAttempts, QuizResponses
* Discussions, DiscussionPosts

---

## ⚡ Getting Started

### Prerequisites

* Java 21+ (recommend latest LTS)
* MySQL 8+
* Maven or Gradle
* VS Code (with Spring Boot & Java extensions)

### Setup

1. **Clone Repository**

   ```bash
   git clone https://github.com/yourusername/lms.git
   cd lms
   ```

2. **Configure Database**
   Create a MySQL database:

   ```sql
   CREATE DATABASE lms_db;
   ```

   Update `application.properties` or `application.yml`:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/lms_db
   spring.datasource.username=root
   spring.datasource.password=yourpassword

   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   ```

3. **Run Application**

   ```bash
   ./mvnw spring-boot:run
   ```

   The app runs on 👉 [http://localhost:8080](http://localhost:8080)

---

## 🔐 Authentication & Authorization

* **JWT-based authentication**
* **Role & Permission checks** with:

  ```java
  @PreAuthorize("hasRole('TEACHER')")
  @PreAuthorize("hasAuthority('upload_lesson')")
  ```
* Frontend can use the `/me` endpoint to get user roles & permissions and hide/show UI accordingly.

---

## 🧪 Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test classes
./mvnw test -Dtest="**/*EntityTest"

# Run tests with verbose output
./mvnw test -Dtest="AcademicYearEntityTest"
```

### Test Coverage

This project includes comprehensive test coverage using **JaCoCo**:

```bash
# Basic coverage report
./run-coverage.sh basic

# Coverage with quality gates
./run-coverage.sh check

# Entity tests only with coverage
./run-coverage.sh entity

# Open HTML coverage report
./run-coverage.sh report
```

**Coverage Report Location:** `target/site/jacoco/index.html`

**Current Test Status:**
- ✅ **Entity Layer**: 12 test classes with 77+ tests
- ⚠️ **Service Layer**: Needs expansion
- ⚠️ **Controller Layer**: Needs expansion
- ⚠️ **Repository Layer**: Needs expansion

For detailed coverage instructions, see: [COVERAGE.md](./COVERAGE.md)

### Test Structure

```
src/test/java/com/mphoola/e_empuzitsi/
│── entity/              # Entity JPA tests (✅ Complete)
│   │── AcademicYearEntityTest.java
│   │── UserEntityTest.java
│   │── SubjectEntityTest.java
│   │── QuizEntityTest.java
│   │── PermissionEntityTest.java
│   │── RoleEntityTest.java
│   │── ... (12 total entity tests)
│── service/             # Service layer tests (🚧 TODO)
│── controller/          # Controller tests (🚧 TODO)
│── repository/          # Repository tests (🚧 TODO)
```

---

## 📌 Roadmap

* [ ] Subject registration APIs
* [ ] Quiz engine with randomization
* [ ] Teacher dashboards
* [ ] Student progress reports
* [ ] REST API documentation (Swagger/OpenAPI)

---

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/awesome-feature`)
3. Commit your changes (`git commit -m 'Add new feature'`)
4. Push to the branch (`git push origin feature/awesome-feature`)
5. Open a Pull Request

---

## 📜 License

This project is licensed under the **MIT License** – see the [LICENSE](./LICENSE) file for details.
