# 🎓 E-Empuzitsi LMS — AI Coding Assistant Guide

This guide provides essential patterns and conventions for this Spring Boot-based Learning Management System targeting secondary schools in Malawi.

## 🏗️ Architecture Overview

This is a **role-based permission system** similar to Laravel Spatie, with fine-grained access control. Key domain concepts:

- **Academic Years** scope all educational content and user interactions
- **Role-Permission System**: Users get roles (STUDENT, TEACHER, ADMIN), roles have permissions
- **Subject-centric learning**: Students register for subjects, teachers manage lessons/quizzes
- **Activity Logging**: System tracks user actions with `@Loggable` annotation

## 📁 Project Structure (Package-by-Layer)

```
src/main/java/com/mphoola/e_empuzitsi/
├── config/          # Security, JWT, Web, Email, Activity Log configs
├── controller/      # REST controllers with @PreAuthorize
├── dto/             # Request/Response DTOs (never expose entities)
├── entity/          # JPA entities with Lombok & audit fields
├── exception/       # Global exception handlers with ApiResponse
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, UserDetailsService, @AllowUnverifiedEmail
├── service/         # Business logic services (@Transactional)
├── services/        # Additional services (deprecated, use service/)
├── util/            # Helpers (JwtUtil, ApiResponse)
├── annotation/      # Custom annotations (@Loggable)
├── interceptor/     # Method interceptors
├── jobs/            # Background jobs
├── listener/        # JPA entity listeners (ActivityLogEntityListener)
└── mail/            # Email services
```

## 🔑 Security & Authorization

**JWT-based auth** with method-level permissions:

```java
@PreAuthorize("hasAuthority('assign_user_role')")
public ResponseEntity<Map<String, Object>> assignRoleToUser(...)

// Roles are prefix-free: ADMIN, STUDENT, TEACHER
// NOT: ROLE_ADMIN
```

**Key Security Classes:**
- `JwtAuthenticationFilter` - JWT token validation
- `CustomUserDetailsService` - Load user with roles/permissions
- `JwtUtil` - Token generation/validation
- `@AllowUnverifiedEmail` - Bypass email verification for certain endpoints

## 🏛️ Entity Design Patterns

**All entities use:**
```java
@Entity
@Table(name = "entity_name")
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EntityName {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Audit fields (automatically managed)
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

**Key relationship patterns:**
- Junction entities: `UserRole`, `StudentSubject` (composite keys with `@IdClass`)
- Always `fetch = FetchType.LAZY` for relationships
- Use `cascade = CascadeType.ALL` only when appropriate

## 🎮 Controller-Service-Repository Pattern

**Controllers are thin:**
```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    
    @PostMapping
    @PreAuthorize("hasAuthority('add_user')")
    @Operation(summary = "Create a new user")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ApiResponse.created(response);
    }
}
```

**Services have business logic:**
```java
@Service
@Transactional
public class UserService {
    
    // Constructor injection (no @Autowired)
    public UserService(UserRepository userRepository, ...) {
        this.userRepository = userRepository;
    }
    
    // Always use @Transactional(readOnly = true) for read operations
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }
}
```

**Always use ApiResponse utility:**
```java
return ApiResponse.success("Users retrieved successfully", response);
return ApiResponse.created(response);
return ApiResponse.success("User deleted successfully");
```

## 🧪 Testing Conventions

**Entity Tests** (`@SpringBootTest @Transactional @ActiveProfiles("test")`):
```java
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserEntityTest {
    @Autowired private EntityManager entityManager;
    // Test entity creation, relationships, constraints
}
```

**Repository Tests** (`@DataJpaTest @ActiveProfiles("test")`):
```java
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {
    @Autowired private TestEntityManager entityManager;
    @Autowired private UserRepository userRepository;
    // Test custom query methods
}
```

**Controller Tests** (`@WebMvcTest` for unit, `@SpringBootTest @AutoConfigureMockMvc` for integration):
```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    
    @Test
    @WithMockUser(authorities = "list_users")
    void getAllUsers_ShouldReturnPagedResult() throws Exception {
        // Test with mock security context
    }
}
```

**Test Database:** H2 in-memory (see `application-test.properties`)

## 🔧 Development Workflows

**Build & Run:**
```bash
./mvnw clean compile           # Compile only
./mvnw spring-boot:run        # Run application
./mvnw test                   # Run all tests
```

**Test Coverage (JaCoCo):**
```bash
./mvnw test jacoco:report     # Generate coverage report
# View: target/site/jacoco/index.html
# Target: 90% instruction, 85% branch coverage
```

**Database Management:**
- Production: MySQL 8+ with `spring.jpa.hibernate.ddl-auto=update`
- Tests: H2 in-memory with `ddl-auto=create-drop`
- Naming: snake_case tables/columns, CamelCase entities

## 🎯 Project-Specific Patterns

**Academic Year Scoping:** Most entities are scoped to academic years for proper data isolation.

**Activity Logging:** Use `@Loggable` annotation on entities to auto-track changes:
```java
@Entity
@Loggable(logName = "user", excludeFields = {"password", "resetToken"})
public class User { ... }
```

**Email Integration:** Mailtrap for development, configurable SMTP for production.

**Error Handling:** Global exception handler with structured JSON responses via `ApiResponse`.

**API Documentation:** OpenAPI 3/Swagger at `/swagger-ui.html`

## 🚨 Critical Conventions

1. **Never expose entities** in controllers - always use DTOs
2. **Always use `@PreAuthorize`** with specific permissions, not roles
3. **Constructor injection** only - no `@Autowired` fields
4. **`@Transactional(readOnly = true)`** for all read operations
5. **Pageable support** for list endpoints
6. **`ApiResponse.success/created/error`** for all controller responses
7. **Lombok `@Builder`** pattern for entity/DTO creation
8. **`@ActiveProfiles("test")`** for all test classes
