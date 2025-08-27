# 📖 Spring Boot Best Practices — Rules for LMS Project

## 1. Project Structure (Package-by-Layer)

Use **package-by-layer** inside the main `com.mphoola.e_empuzitsi` package:

```
src/main/java/com/mphoola/e_empuzitsi
├── config          # Security, Web, DB, etc.
├── controller      # REST controllers
├── dto             # Request/response DTOs
├── entity          # JPA entities (database models)
├── exception       # Global exception handlers
├── repository      # JPA repositories
├── security        # Security config, JWT, roles/permissions
├── service         # Business logic services
└── util            # Helpers, mappers
```

---

## 2. Naming Conventions

### **Java Classes**

* **Entities**: Singular, PascalCase → `User`, `Subject`, `Lesson`.
* **Repositories**: Suffix with `Repository` → `UserRepository`.
* **Services**: Suffix with `Service` → `UserService`.
* **Controllers**: Suffix with `Controller` → `UserController`.
* **DTOs**: Suffix with `Request` or `Response` → `UserRequest`, `UserResponse`.
* **Exceptions**: Suffix with `Exception` → `ResourceNotFoundException`.

### **Variables & Methods**

* Variables → **camelCase** → `firstName`, `academicYearId`.
* Methods → **camelCase** → `getUserById()`, `registerStudent()`.
* Constants → **UPPER\_SNAKE\_CASE** → `MAX_ATTEMPTS`.

### **Database**

* Tables → **snake\_case plural** → `users`, `subjects`, `lessons`.
* Columns → **snake\_case singular** → `first_name`, `academic_year_id`.
* Join tables → `parent_child` → `student_subject`, `quiz_attempts`.
* Primary keys → always `id` (auto-increment or UUID).
* Foreign keys → `<entity>_id` → `user_id`, `subject_id`.

---

## 3. Entities & JPA

* Use **Lombok**:

  * `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.
* Always define `@Table(name = "table_name")`.
* Use `@ManyToOne(fetch = LAZY)` by default.
* Explicitly set `cascade` only when necessary.
* Always annotate relationships both sides where relevant.

---

## 4. DTOs & Mappers

* Never expose entities directly in controllers.
* Always create `RequestDTO` and `ResponseDTO`.
* Use **MapStruct** or manual mapping utils for `Entity ↔ DTO`.

---

## 5. Services & Business Logic

* Services contain business logic, controllers should be thin.
* Each service has an interface + implementation (`UserService`, `UserServiceImpl`).
* No business logic in controllers.

---

## 6. Security

* Use **Spring Security + JWT**.
* Permissions:

  ```java
  @PreAuthorize("hasAuthority('upload_lesson')")
  ```
* Roles should be **prefix-free** (`ADMIN`, `STUDENT`, `TEACHER`) instead of `ROLE_ADMIN`.

---

## 7. Exceptions

* Use `@ControllerAdvice` with a `GlobalExceptionHandler`.
* Always return structured JSON errors.

---

## 8. Logging

* Use SLF4J, never `System.out.println`.
* Logger name → `private static final Logger log = LoggerFactory.getLogger(MyClass.class);`.
* Logging levels:

  * INFO → normal operations
  * WARN → unusual but handled cases
  * ERROR → failures

---

## 9. Testing

* JUnit 5 + Spring Boot Test.
* Use **Testcontainers** for MySQL in integration tests.
* Use `@DataJpaTest` for repository tests.
* Use `@WebMvcTest` for controller tests.

---

## 10. API Design

* REST conventions:

  * `GET /api/users` → list
  * `GET /api/users/{id}` → details
  * `POST /api/users` → create
  * `PUT /api/users/{id}` → update
  * `DELETE /api/users/{id}` → delete
* Return `ResponseEntity<T>`.
* Support pagination with `Pageable`.

---

## 11. Performance & Scalability

* Use DTO projections for large queries.
* Use Spring Cache/Redis for frequently used queries.
* Mark long-running tasks with `@Async`.
* Always prefer batch inserts/updates for bulk operations.
