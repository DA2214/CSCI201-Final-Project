-- Create the database
DROP DATABASE IF EXISTS GymSync;
CREATE DATABASE GymSync;
USE GymSync;

--------------------------------------------------
-- 1. USERS TABLE
--------------------------------------------------
CREATE TABLE users (
    uid INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

--------------------------------------------------
-- 2. MACHINE TABLE
--------------------------------------------------
CREATE TABLE Machines (
    machineId INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status ENUM('AVAILABLE', 'RESERVED', 'IN_USE') NOT NULL DEFAULT 'AVAILABLE',
    INDEX idx_status (status),
    INDEX idx_type (type)
);

--------------------------------------------------
-- 4. WORKOUT HISTORY TABLE
--------------------------------------------------
-- Renamed from 'reservations' for clarity - stores completed workout sessions
CREATE TABLE workout_history (
    reservationId INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL,
    machineID VARCHAR(50) NOT NULL,  -- Stores machine name, JOINs with Machines.name
    startTime TIME NOT NULL,
    endTime TIME NOT NULL,
    status VARCHAR(50) NOT NULL,
    date DATE NOT NULL,

    FOREIGN KEY (userID) REFERENCES users(uid)
    -- Updated to use new Machines table: foreign key removed, JOIN uses Machines.name
);

--------------------------------------------------
-- 5. WAITLIST TABLE
--------------------------------------------------
CREATE TABLE waitlist (
    waitID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL,
    machineID VARCHAR(50) NOT NULL,  -- Stores machine name, references Machines.name
    requestTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notified TINYINT(1) DEFAULT 0,
    FOREIGN KEY (userID) REFERENCES users(uid)
    -- Updated to use new Machines table: foreign key removed, uses machine names
);

--------------------------------------------------
-- 6. NOTIFICATION TABLE
--------------------------------------------------
CREATE TABLE notifications (
    notifyID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL,
    message TEXT NOT NULL,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    readStatus TINYINT(1) DEFAULT 0,

    FOREIGN KEY (userID) REFERENCES users(uid)
);

