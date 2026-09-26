-- ========================================================
-- Campus Lost & Found System SQLite Database Schema
-- Database File: lost_found_db.db (Serverless Embedded SQLite)
-- ========================================================

PRAGMA foreign_keys = ON;

-- --------------------------------------------------------
-- Table structure for table `users`
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'USER',
    full_name TEXT,
    email TEXT,
    phone TEXT
);

-- --------------------------------------------------------
-- Table structure for table `items` with Foreign Key
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    type TEXT NOT NULL, -- 'LOST' or 'FOUND'
    title TEXT NOT NULL,
    category TEXT,
    location TEXT,
    description TEXT,
    image_path TEXT,
    reporter_name TEXT,
    phone TEXT,
    email TEXT,
    status TEXT NOT NULL DEFAULT 'OPEN', -- 'OPEN' or 'RETURNED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- --------------------------------------------------------
-- Demonstration Default Users
-- Admin: admin / admin
-- User:  user / user
-- --------------------------------------------------------
INSERT OR IGNORE INTO users (id, username, password, role, full_name, email, phone) VALUES
(1, 'admin', 'admin', 'ADMIN', 'Campus Administrator', 'admin@campus.edu', '01710000001'),
(2, 'user', 'user', 'USER', 'Student User', 'student@campus.edu', '01710000002');

-- --------------------------------------------------------
-- Sample Initial Data for `items`
-- Demonstrates Lost, Found, Auto-Matching, and Filtering
-- --------------------------------------------------------
INSERT OR IGNORE INTO items (id, user_id, type, title, category, location, description, image_path, reporter_name, phone, email, status) VALUES
(1, 2, 'LOST', 'Blue Hydro Flask Water Bottle', 'Bottles', 'Library 2nd Floor', '32oz navy blue Hydro Flask with stickers on the side.', NULL, 'John Doe', '555-0101', 'john.doe@example.com', 'OPEN'),
(2, 1, 'FOUND', 'Blue Hydro Flask Bottle', 'Bottles', 'Library Reading Room', 'Found a navy blue metal water bottle near the study desks.', NULL, 'Jane Smith', '555-0102', 'jane.smith@example.com', 'OPEN'),
(3, 2, 'LOST', 'Dell XPS 13 Laptop Charger', 'Electronics', 'Computer Lab 3', 'Black 65W USB-C Dell charger left on workbench B.', NULL, 'Alice Johnson', '555-0103', 'alice.j@example.com', 'OPEN'),
(4, 1, 'FOUND', 'USB-C Laptop Power Adapter', 'Electronics', 'Computer Lab 3', 'Dell Type-C black charger found plugged into desk wall socket.', NULL, 'Campus Security', '555-0199', 'security@example.com', 'OPEN'),
(5, 2, 'LOST', 'Scientific Calculator Casio fx-991EX', 'Stationery', 'Room 204', 'Casio classwiz calculator with name initials SJ on cover.', NULL, 'Samuel Green', '555-0104', 'samuel.g@example.com', 'RETURNED'),
(6, 1, 'FOUND', 'Campus ID Card', 'ID Cards', 'Cafeteria', 'Student identity card belonging to Engineering department.', NULL, 'Cafeteria Staff', '555-0105', 'staff@example.com', 'RETURNED');
