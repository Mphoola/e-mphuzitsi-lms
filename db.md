

# 📚 E-Learning Platform Database Design (Secondary School LMS)

This document describes the database structure for the platform.

---

## 1. Users

* **id** (PK)
* **name**
* **email** (unique)
* **password**

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
* **name** (unique, e.g. upload\_lesson, grade\_quiz, manage\_users)

### Relationships

* A `Permission` can belong to many `Roles` (via **RolePermissions**).
* A `Permission` can also be granted directly to `Users` (via **UserPermissions**).

---

## 4. UserRoles (Join Table)

* **user\_id** (FK → Users)
* **role\_id** (FK → Roles)
  **PK** → (user\_id, role\_id)

---

## 5. RolePermissions (Join Table)

* **role\_id** (FK → Roles)
* **permission\_id** (FK → Permissions)
  **PK** → (role\_id, permission\_id)

---

## 6. UserPermissions (Join Table)

* **user\_id** (FK → Users)
* **permission\_id** (FK → Permissions)
  **PK** → (user\_id, permission\_id)

---

## 7. AcademicYears

* **id** (PK)
* **year** (e.g. 2025)

### Relationships

* Linked to `StudentSubjects`
* Linked to `QuizAttempts`
* Linked to `Discussions`

---

## 8. Subjects

* **id** (PK)
* **name**

### Relationships

* A `Subject` has many `LessonComponents`.
* A `Subject` has many `StudentSubjects`.
* A `Subject` has many `Discussions`.

---

## 9. StudentSubjects (Join Table)

* **id** (PK)
* **student\_id** (FK → Users)
* **subject\_id** (FK → Subjects)
* **academic\_year\_id** (FK → AcademicYears)

### Purpose

Tracks which subject a student is taking in a given academic year.

---

## 10. LessonComponents

* **id** (PK)
* **title**
* **type** (VIDEO, PDF, QUIZ)
* **subject\_id** (FK → Subjects)

### Relationships

* A `LessonComponent` can have many `CourseContents`.
* A `LessonComponent` can link to a `Quiz`.

---

## 11. CourseContents

* **id** (PK)
* **file\_url**
* **description**
* **lesson\_component\_id** (FK → LessonComponents)

---

## 12. Quizzes

* **id** (PK)
* **title**
* **lesson\_component\_id** (FK → LessonComponents)

### Relationships

* A `Quiz` has many `QuizQuestions`.
* A `Quiz` can be attempted by many `Students`.

---

## 13. QuizQuestions

* **id** (PK)
* **question\_text**
* **quiz\_id** (FK → Quizzes)

### Relationships

* A `QuizQuestion` has many `QuizOptions`.

---

## 14. QuizOptions

* **id** (PK)
* **option\_text**
* **is\_correct** (boolean)
* **question\_id** (FK → QuizQuestions)

---

## 15. QuizAttempts

* **id** (PK)
* **student\_id** (FK → Users)
* **quiz\_id** (FK → Quizzes)
* **academic\_year\_id** (FK → AcademicYears)
* **score**

### Relationships

* A `QuizAttempt` has many `QuizResponses`.

---

## 16. QuizResponses

* **id** (PK)
* **attempt\_id** (FK → QuizAttempts)
* **question\_id** (FK → QuizQuestions)
* **chosen\_option\_id** (FK → QuizOptions)

---

## 17. Discussions

* **id** (PK)
* **topic**
* **subject\_id** (FK → Subjects)
* **academic\_year\_id** (FK → AcademicYears)

### Relationships

* A `Discussion` has many `DiscussionPosts`.

---

## 18. DiscussionPosts

* **id** (PK)
* **content**
* **discussion\_id** (FK → Discussions)
* **author\_id** (FK → Users)

---

# 🔗 Summary of Key Relationships

* **User ↔ Subject** → via `StudentSubjects`
* **User ↔ Role ↔ Permission** → via `UserRoles`, `RolePermissions`, `UserPermissions`
* **Subject ↔ LessonComponent**
* **LessonComponent ↔ CourseContent / Quiz**
* **Quiz ↔ QuizQuestion ↔ QuizOption**
* **User ↔ QuizAttempt ↔ QuizResponse**
* **Subject ↔ Discussion ↔ DiscussionPost**
* **AcademicYear** ties everything to a specific school year

