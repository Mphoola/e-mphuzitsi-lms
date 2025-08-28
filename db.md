# 📚 E-Learning Platform Database Design (Secondary School LMS)

This document describes the database structure for the E-Empuzitsi Learning Management System platform for Malawi Secondary Schools.

---

## 1. Users

* **id** (PK)
* **name**
* **email** (unique)
* **password**
* **reset_token** (nullable)
* **reset_token_expires_at** (nullable)

### Relationships

* A `User` (student) can register for many `Subjects` (via **StudentSubjects**).
* A `User` can author `DiscussionPosts`.
* A `User` (student) can attempt many `Quizzes`.
* A `User` can have many `Roles` (via **UserRoles**).
* A `User` can have additional direct `Permissions` (via **UserPermissions**).

---

## 2. Roles

* **id** (PK)
* **name** (unique, e.g. STUDENT, TEACHER, ADMIN)

### Relationships

* A `Role` can have many `Permissions` (via **RolePermissions**).
* A `Role` can be assigned to many `Users` (via **UserRoles**).

---

## 3. Permissions

* **id** (PK)
* **name** (unique, e.g. upload_lesson, manage_users, take_quiz)

### Relationships

* A `Permission` can be assigned to many `Roles` (via **RolePermissions**).
* A `Permission` can be directly assigned to `Users` (via **UserPermissions**).

---

## 4. UserRoles (Join Table)

* **user_id** (PK, FK → Users)
* **role_id** (PK, FK → Roles)

Composite primary key linking users to their roles.

---

## 5. RolePermissions (Join Table)

* **role_id** (PK, FK → Roles)
* **permission_id** (PK, FK → Permissions)

Composite primary key linking roles to their permissions.

---

## 6. UserPermissions (Join Table)

* **id** (PK)
* **user_id** (FK → Users)
* **permission_id** (FK → Permissions)

Direct permission assignments to users (beyond role-based permissions).

---

## 7. AcademicYears

* **id** (PK)
* **year** (e.g., 2024, 2025)
* **is_active** (boolean - current active year)

### Relationships

* An `AcademicYear` can have many `StudentSubjects`.
* An `AcademicYear` can have many `QuizAttempts`.

---

## 8. Subjects

* **id** (PK)
* **name** (e.g. Mathematics, Physics, Biology)

### Relationships

* A `Subject` can have many `LessonComponents`.
* A `Subject` can be enrolled by many `Users` (via **StudentSubjects**).
* A `Subject` can have many `Discussions`.

---

## 9. StudentSubjects (Join Table)

* **id** (PK)
* **student_id** (FK → Users)
* **subject_id** (FK → Subjects)
* **academic_year_id** (FK → AcademicYears)

Links students to subjects for specific academic years.

---

## 10. LessonComponents

* **id** (PK)
* **title**
* **type** (ENUM: VIDEO, DOCUMENT, QUIZ, ASSIGNMENT)
* **subject_id** (FK → Subjects)

### Relationships

* A `LessonComponent` belongs to one `Subject`.
* A `LessonComponent` can have many `CourseContents`.
* A `LessonComponent` can have one `Quiz` (if type is QUIZ).

---

## 11. CourseContents

* **id** (PK)
* **file_url**
* **description**
* **lesson_component_id** (FK → LessonComponents)

### Relationships

* A `CourseContent` belongs to one `LessonComponent`.

---

## 12. Quizzes

* **id** (PK)
* **title**
* **lesson_component_id** (FK → LessonComponents, unique)

### Relationships

* A `Quiz` belongs to one `LessonComponent` (one-to-one).
* A `Quiz` can have many `QuizQuestions`.
* A `Quiz` can have many `QuizAttempts`.

---

## 13. QuizQuestions

* **id** (PK)
* **question_text**
* **question_type** (ENUM: MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER)
* **quiz_id** (FK → Quizzes)

### Relationships

* A `QuizQuestion` belongs to one `Quiz`.
* A `QuizQuestion` can have many `QuizOptions` (for multiple choice).
* A `QuizQuestion` can have many `QuizResponses`.

---

## 14. QuizOptions

* **id** (PK)
* **option_text**
* **is_correct** (boolean)
* **quiz_question_id** (FK → QuizQuestions)

### Relationships

* A `QuizOption` belongs to one `QuizQuestion`.

---

## 15. QuizAttempts

* **id** (PK)
* **quiz_id** (FK → Quizzes)
* **student_id** (FK → Users)
* **academic_year_id** (FK → AcademicYears)
* **score** (integer, percentage)

### Relationships

* A `QuizAttempt` belongs to one `Quiz` and one `User` (student).
* A `QuizAttempt` can have many `QuizResponses`.

---

## 16. QuizResponses

* **id** (PK)
* **quiz_question_id** (FK → QuizQuestions)
* **quiz_attempt_id** (FK → QuizAttempts)
* **selected_option_id** (FK → QuizOptions, nullable)
* **answer_text** (nullable, for short answers)

### Relationships

* A `QuizResponse` belongs to one `QuizQuestion` and one `QuizAttempt`.

---

## 17. Discussions

* **id** (PK)
* **title**
* **subject_id** (FK → Subjects)

### Relationships

* A `Discussion` belongs to one `Subject`.
* A `Discussion` can have many `DiscussionPosts`.

---

## 18. DiscussionPosts

* **id** (PK)
* **content**
* **discussion_id** (FK → Discussions)
* **author_id** (FK → Users)
* **parent_post_id** (FK → DiscussionPosts, nullable for replies)

### Relationships

* A `DiscussionPost` belongs to one `Discussion` and one `User` (author).
* A `DiscussionPost` can have many child `DiscussionPosts` (replies).

---

# 🔗 Summary of Key Relationships

## Authentication & Authorization
- **Users** ↔ **Roles** (many-to-many via UserRoles)
- **Roles** ↔ **Permissions** (many-to-many via RolePermissions)  
- **Users** ↔ **Permissions** (many-to-many via UserPermissions)

## Academic Structure
- **Users** (students) ↔ **Subjects** (many-to-many via StudentSubjects)
- **Subjects** → **LessonComponents** (one-to-many)
- **LessonComponents** → **CourseContents** (one-to-many)
- **LessonComponents** → **Quiz** (one-to-one, when type=QUIZ)

## Assessment System  
- **Quiz** → **QuizQuestions** (one-to-many)
- **QuizQuestions** → **QuizOptions** (one-to-many)
- **Users** (students) → **QuizAttempts** (one-to-many)
- **QuizAttempts** → **QuizResponses** (one-to-many)

## Discussion System
- **Subjects** → **Discussions** (one-to-many)
- **Discussions** → **DiscussionPosts** (one-to-many)
- **Users** → **DiscussionPosts** (one-to-many as authors)
- **DiscussionPosts** → **DiscussionPosts** (self-referencing for replies)

---

# 🎯 Usage Examples

## Typical Queries

**Get all subjects for a student in current academic year:**
```sql
SELECT s.* FROM subjects s
JOIN student_subjects ss ON s.id = ss.subject_id
WHERE ss.student_id = ? AND ss.academic_year_id = ?
```

**Get all permissions for a user (via roles + direct):**
```sql
-- Via roles
SELECT DISTINCT p.* FROM permissions p
JOIN role_permissions rp ON p.id = rp.permission_id
JOIN roles r ON rp.role_id = r.id
JOIN user_roles ur ON r.id = ur.role_id
WHERE ur.user_id = ?

UNION

-- Direct permissions
SELECT p.* FROM permissions p
JOIN user_permissions up ON p.id = up.permission_id
WHERE up.user_id = ?
```

**Get quiz results for a student:**
```sql
SELECT q.title, qa.score, qa.created_at
FROM quiz_attempts qa
JOIN quizzes q ON qa.quiz_id = q.id
WHERE qa.student_id = ?
ORDER BY qa.created_at DESC
```

This comprehensive schema supports:
- ✅ Multi-role authentication system
- ✅ Academic year management
- ✅ Subject enrollment and content delivery
- ✅ Quiz system with multiple question types
- ✅ Discussion forums per subject
- ✅ Progress tracking and reporting
- ✅ Password reset functionality
- ✅ Audit trails with timestamps
