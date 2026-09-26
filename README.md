 
# campus_lost_and_found_mangament 

# Campus Lost & Found Management System

[![Java Version](https://img.shields.io/badge/Java-17%20LTS-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17.0.8-FF6600?logo=java&logoColor=white)](https://openjfx.io/)
[![Database](https://img.shields.io/badge/Database-SQLite%203-003B57?logo=sqlite&logoColor=white)](https://www.sqlite.org/)
[![Build Tool](https://img.shields.io/badge/Build-Apache%20Maven-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/Tests-7%20Passed-success?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![License](https://img.shields.io/badge/License-Academic%20Use-blue.svg)](#)

A modern, standalone desktop application built with **Java 17**, **JavaFX 17**, **SQLite (JDBC)**, and **REST API Networking**. Tailored for campus environments (such as **KUET**), this platform provides students and administrators with a centralized, secure ecosystem to report, search, auto-match, and safely recover lost belongings.

---

## 📑 Table of Contents
1. [🔐 Demo Login Credentials](#-demo-login-credentials)
2. [🏆 Evaluation Rubric Compliance Matrix](#-evaluation-rubric-compliance-matrix)
3. [🧱 System Architecture & Design Patterns](#-system-architecture--design-patterns)
4. [⚡ Concurrency & Multi-Threading](#-concurrency--multi-threading)
5. [🌐 Networking & Live JSON Data Parsing](#-networking--live-json-data-parsing)
6. [🗄 SQLite Database Design & Relationships](#-sqlite-database-design--relationships)
7. [📐 Layout Responsiveness & Modern UI](#-layout-responsiveness--modern-ui)
8. [🔄 Complete CRUD Workflow](#-complete-crud-workflow)
9. [🚀 Key Features](#-key-features)
10. [▶️ How to Build, Test & Run](#️-how-to-build-test--run)
11. [🗂 Project Directory Structure](#-project-directory-structure)
12. [📜 Git Commit History Timeline](#-git-commit-history-timeline)
13. [👨‍💻 Author & Academic Information](#-author--academic-information)

---

## 🔐 Demo Login Credentials

The application comes pre-seeded with sample user accounts directly in SQLite. You can sign in immediately:

| Role | Username | Password | Access Level & Responsibilities |
|:---:|:---:|:---:|---|
| 👑 **Administrator** | `admin` | `admin` | Full administrative control, live analytics with `PieChart`, contact disclosure, item status lifecycle (`OPEN` $\rightarrow$ `RETURNED`), auto-match scoring, and permanent deletion. |
| 🎓 **Student / User** | `user` | `user` | Student dashboard, real-time campus weather advisory, report lost items, report found items, and interactive community items catalog. |

> [!NOTE]
> All demonstration accounts and sample data are automatically initialized on the first application launch via [`DatabaseInitializer.java`](src/main/java/app/util/DatabaseInitializer.java). No manual database setup is necessary.

---

## 🧱 System Architecture & Design Patterns

The application is structured into a clean **3-Tier Layered Architecture** ensuring separation of concerns:

```text
       ┌──────────────────────────────────────────────────────────────┐
       │                 PRESENTATION LAYER (JavaFX UI)               │
       │  • LoginView (Secure Authentication)                         │
       │  • UserDashboardLayout & AdminDashboardLayout                │
       │  • CampusWeatherWidget (Live REST API & StackPane)           │
       │  • ViewItemsView (Async FlowPane Catalog & Modal Details)    │
       │  • AdminManageItemsView (CRUD Controls & Filtering)          │
       └──────────────────────────────┬───────────────────────────────┘
                                      │
       ┌──────────────────────────────▼───────────────────────────────┐
       │                 SERVICE & CONCURRENCY LAYER                  │
       │  • ItemService: Auto-match heuristic algorithm & async Tasks │
       │  • CampusWeatherService: HTTP REST client for live JSON data │
       │  • AppThreadPool: 4-thread ExecutorService worker pool       │
       └──────────────────────────────┬───────────────────────────────┘
                                      │
       ┌──────────────────────────────▼───────────────────────────────┐
       │                     DAO LAYER (Data Access)                  │
       │  • DAOFactory: Decoupled interface-driven instantiation      │
       │  • SqliteItemDAO & SqliteUserDAO: Relational SQL operations  │
       │  • JsonUserDAO & JsonDatabaseManager: Fallback persistence   │
       └──────────────────────────────┬───────────────────────────────┘
                                      │
       ┌──────────────────────────────▼───────────────────────────────┐
       │                   DATABASE & PERSISTENCE                     │
       │  • SQLite Database: lost_found_db.db (Foreign Keys enabled)  │
       │  • DatabaseInitializer: Automated DDL & initial seed data    │
       └──────────────────────────────────────────────────────────────┘
```

### Applied Design Patterns:
- **DAO Pattern (Data Access Object):** [`ItemDAO`](src/main/java/app/dao/ItemDAO.java) and [`UserDAO`](src/main/java/app/dao/UserDAO.java) decouple high-level UI/business logic from underlying database SQL commands.
- **Factory Pattern:** [`DAOFactory.java`](src/main/java/app/dao/DAOFactory.java) centralizes DAO instance creation, making the storage engine easily swappable.
- **Observer / Listener Pattern:** JavaFX property listeners (`searchField.textProperty().addListener(...)`) trigger real-time filtering without explicit refresh buttons.

---

## ⚡ Concurrency & Multi-Threading

To prevent UI freezes and deliver a smooth user experience, background processing is handled via **Multi-threading**:

1. **Dedicated Worker Thread Pool:**
   [`AppThreadPool.java`](src/main/java/app/util/AppThreadPool.java) creates a managed `ThreadPoolExecutor`:
   ```java
   private static final ExecutorService executor = Executors.newFixedThreadPool(4, (r) -> {
       Thread thread = new Thread(r, "LostFound-Worker-" + threadCounter.getAndIncrement());
       thread.setDaemon(true);
       return thread;
   });
   ```

2. **Asynchronous JavaFX Task Execution:**
   When loading items in [`ViewItemsView.java`](src/main/java/app/ui/ViewItemsView.java), a JavaFX `Task<List<Item>>` executes on the thread pool while a `ProgressIndicator` is shown:
   ```java
   ItemService.getAllItemsAsync(items -> {
       // Executed safely back on JavaFX Application Thread
       centerStack.getChildren().clear();
       for (Item item : items) {
           grid.getChildren().add(createItemTile(stage, item));
       }
   }, error -> {
       // Error handling
   });
   ```

---

## 🌐 Networking & Live JSON Data Parsing

The application connects to the internet via **HTTP REST API** to retrieve real-time weather conditions for the campus and produce smart lost & found recovery advisories:

1. **HTTP Client (`java.net.http.HttpClient`):**
   Sends non-blocking HTTP GET requests to `https://api.open-meteo.com/v1/forecast?latitude=22.8998&longitude=89.5024&current_weather=true`.

2. **Jackson JSON Tree Model Parsing:**
   [`CampusWeatherService.java`](src/main/java/app/service/CampusWeatherService.java) extracts structured fields from the JSON payload:
   ```java
   JsonNode root = objectMapper.readTree(response.body());
   JsonNode current = root.path("current_weather");
   double temp = current.path("temperature").asDouble();
   double wind = current.path("windspeed").asDouble();
   int code = current.path("weathercode").asInt();
   ```

3. **Intelligent Advisory Engine:**
   - Detects precipitation ($51 \le \text{code} \le 99$) $\rightarrow$ Alerts users that outdoor lost items are vulnerable to water damage.
   - Detects high temperatures ($> 34^\circ\text{C}$) $\rightarrow$ Advises search hydration.
   - Offline Safe: Automatically provides a cached fallback if no internet connection is detected.

---

## 🗄 SQLite Database Design & Relationships

The database runs out of a local, embedded file [`lost_found_db.db`](lost_found_db.db). **No MySQL or database server installation is needed.**

### Entity Relationship (ER) Schema:
```text
  ┌────────────────────────┐           ┌────────────────────────┐
  │         users          │           │         items          │
  ├────────────────────────┤           ├────────────────────────┤
  │ PK  id (INTEGER)       │ 1       N │ PK  id (INTEGER)       │
  │     username (TEXT)    │◄──────────┼─FK  user_id (INTEGER)  │
  │     password (TEXT)    │           │     type (TEXT)        │
  │     role (TEXT)        │           │     title (TEXT)       │
  │     full_name (TEXT)   │           │     category (TEXT)    │
  │     email (TEXT)       │           │     location (TEXT)    │
  │     phone (TEXT)       │           │     description (TEXT) │
  └────────────────────────┘           │     image_path (TEXT)  │
                                       │     reporter_name(TEXT)│
                                       │     phone (TEXT)       │
                                       │     email (TEXT)       │
                                       │     status (TEXT)      │
                                       │     created_at (TIME)  │
                                       └────────────────────────┘
```

- **Foreign Key Constraints:** Enabled at runtime with `PRAGMA foreign_keys = ON;`.
- **Relational Integrity:** `FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL`.
- **Relational SQL Queries:**
  ```sql
  SELECT items.*, users.username AS linked_user 
  FROM items 
  LEFT JOIN users ON items.user_id = users.id 
  ORDER BY items.id DESC;
  ```

---

## 📐 Layout Responsiveness & Modern UI

The user interface uses JavaFX property bindings to ensure views dynamically adapt to any window size:
- **Search Bar Responsiveness:** `searchField.prefWidthProperty().bind(root.widthProperty().multiply(0.35));`
- **Dynamic Scroll Areas:** `scrollPane.prefHeightProperty().bind(root.heightProperty().subtract(topArea.heightProperty()).subtract(130));`
- **Adaptive Info Cards:** `card.prefWidthProperty().bind(container.widthProperty().subtract(80).divide(3));`
- **Layered UI Overlays:** `StackPane` centers progress spinners, badges, and alerts over parent views.

---

## 🔄 Complete CRUD Workflow

| Operation | Action in UI | Database Implementation |
|---|---|---|
| **Create** | Student submits "Report Lost Item" or "Report Found Item" form. | `SqliteItemDAO.addItem()` runs an SQL `INSERT INTO items` with generated key retrieval. |
| **Read** | User/Admin views catalog, searches items, or filters by status. | `SqliteItemDAO.getAllItems()` runs an SQL `SELECT` with `LEFT JOIN users`. |
| **Update** | Admin clicks "Mark Returned" on verified item. | `SqliteItemDAO.updateItemStatus()` runs `UPDATE items SET status = 'RETURNED' WHERE id = ?`. |
| **Delete** | Admin clicks "Delete" on returned item after confirmation. | `SqliteItemDAO.deleteItem()` runs `DELETE FROM items WHERE id = ?`. |

---

## 🚀 Key Features

### 🔍 Smart Auto-Match Algorithm
The administrator panel features an automated heuristic matching engine that cross-references lost items with found items:
- **Category Match:** $+3$ points
- **Location Substring Match:** $+2$ points
- **Title Keyword Overlap:** $+2$ points
- **Description Similarity:** $+1$ point
- Items scoring $\ge 5$ points are highlighted as potential matches, speeding up the recovery process.

### 📊 Administrative Analytics
- Interactive JavaFX `PieChart` visualizes real-time distribution across **Lost**, **Found**, and **Returned** items.
- Live summary counters (Total Reports, Open Cases, Returned Ratio).

---

## ▶️ How to Build, Test & Run

### Prerequisites
- **Java Development Kit (JDK):** Version 17 or higher (Oracle JDK, Eclipse Temurin, or Microsoft OpenJDK)
- **Apache Maven:** Version 3.8 or higher

### Maven Commands

1. **Compile the Application:**
   ```bash
   mvn clean compile
   ```

2. **Execute Automated Unit Tests:**
   ```bash
   mvn test
   ```
   *(Runs all 7 unit tests verifying models, concurrency thread pool, JSON parser, and SQLite foreign key schemas)*

3. **Launch the JavaFX GUI:**
   ```bash
   mvn javafx:run
   ```

---

## 🗂 Project Directory Structure

```text
LostAndFoundFX/
│
├── database/
│   └── lost_found_db.sql               # Pure SQLite schema DDL with Foreign Keys
│
├── src/
│   ├── main/
│   │   ├── java/app/
│   │   │   ├── MainApp.java                    # JavaFX entry point
│   │   │   ├── config/
│   │   │   │   └── DatabaseConfig.java         # Connection properties loader
│   │   │   ├── dao/
│   │   │   │   ├── DAOFactory.java             # Factory returning DAO implementations
│   │   │   │   ├── ItemDAO.java                # Item CRUD interface
│   │   │   │   ├── SqliteItemDAO.java          # Relational SQLite item repository
│   │   │   │   ├── UserDAO.java                # User authentication interface
│   │   │   │   ├── SqliteUserDAO.java          # Relational SQLite user repository
│   │   │   │   └── JsonUserDAO.java            # Local JSON fallback repository
│   │   │   ├── model/
│   │   │   │   ├── Item.java                   # Abstract base item model
│   │   │   │   ├── LostItem.java               # Concrete subclass for lost items
│   │   │   │   ├── FoundItem.java              # Concrete subclass for found items
│   │   │   │   ├── User.java                   # Base user model
│   │   │   │   └── Admin.java                  # Administrator model
│   │   │   ├── service/
│   │   │   │   ├── ItemService.java            # Business logic, scoring, and async tasks
│   │   │   │   └── CampusWeatherService.java   # HTTP REST client & Jackson JSON parser
│   │   │   ├── ui/
│   │   │   │   ├── LoginView.java              # Sign-in view with PasswordField
│   │   │   │   ├── UserDashboardLayout.java    # Responsive user dashboard
│   │   │   │   ├── AdminDashboardLayout.java   # Responsive admin dashboard with PieChart
│   │   │   │   ├── CampusWeatherWidget.java    # Real-time weather card (StackPane)
│   │   │   │   ├── AdminManageItemsView.java   # Item management with CRUD controls
│   │   │   │   ├── ReportLostView.java         # Form to report lost items
│   │   │   │   ├── ReportFoundView.java        # Form to report found items
│   │   │   │   └── ViewItemsView.java          # Async catalog view
│   │   │   └── util/
│   │   │       ├── AppThreadPool.java          # Concurrency ThreadPoolExecutor
│   │   │       ├── DatabaseConnection.java     # SQLite JDBC connection manager
│   │   │       ├── DatabaseInitializer.java    # Schema migration & initial data seeder
│   │   │       └── JsonDatabaseManager.java    # Low-level Jackson helper
│   │   └── resources/
│   │       ├── db.properties                   # Active database configuration
│   │       ├── db.properties.example           # Configuration template
│   │       └── styles.css                      # Modern CSS styling
│   │
│   └── test/
│       └── java/app/
│           └── ModelAndServiceTest.java        # 7 Unit tests covering all rubrics
│
├── lost_found_db.db                            # SQLite database file
├── users.json                                  # Fallback user storage
├── pom.xml                                     # Maven project descriptor
├── .gitignore                                  # Git ignore rules
└── README.md                                   # Project documentation
```

---

## 📜 Git Commit History Timeline

Commit history starts from the project idea submission date (**September 6th**) and progresses through each module:

```text
* af0b447 - sadman-sakib-420 (2026-09-26) : docs: update README with credentials and rubric matrix
* c5aa680 - sadman-sakib-420 (2026-09-26) : docs(sqlite): standardize documentation and schema entirely on SQLite
* 92402f3 - sadman-sakib-420 (2026-09-26) : feat(network): implement live HTTP REST weather API with Jackson parsing
* 1cc2360 - sadman-sakib-420 (2026-09-23) : feat(concurrency): implement background thread pool and async JavaFX Tasks
* 699a52e - sadman-sakib-420 (2026-09-18) : feat(db): integrate SQLite database with relational foreign key tables
* cc34282 - sadman-sakib-420 (2026-09-12) : feat(ui): implement JavaFX authentication, report forms, and UI layouts
* 731346a - sadman-sakib-420 (2026-09-06) : feat(core): initial project idea submission and core OOP domain models
```

---

## 👨‍💻 Author & Academic Information

- **Developer:** [sadman-sakib-420](https://github.com/sadman-sakib-420)
- **Email:** `sakibsadman420@outlook.com`
- **Institution:** Khulna University of Engineering & Technology (KUET)
