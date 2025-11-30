-- Create the database
DROP DATABASE IF EXISTS TrojanTracker;
CREATE DATABASE TrojanTracker;
USE TrojanTracker;

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
CREATE TABLE machines (
    machineID VARCHAR(50) PRIMARY KEY,
    machineName VARCHAR(100) NOT NULL
);

--------------------------------------------------
-- 3. MACHINE USAGE TABLE
--------------------------------------------------
CREATE TABLE machineusage (
    usageID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL,
    machineID VARCHAR(50) NOT NULL,
    duration INT NOT NULL,
    date DATE NOT NULL,

    FOREIGN KEY (userID) REFERENCES users(uid),
    FOREIGN KEY (machineID) REFERENCES machines(machineID)
);

--------------------------------------------------
-- 4. RESERVATION TABLE
--------------------------------------------------
CREATE TABLE reservations (
    reservationId INT AUTO_INCREMENT PRIMARY KEY,
    userID INT NOT NULL,
    machineID VARCHAR(50) NOT NULL,
    startTime TIME NOT NULL,
    endTime TIME NOT NULL,
    status VARCHAR(50) NOT NULL,
    date DATE NOT NULL,

    FOREIGN KEY (userID) REFERENCES users(uid),
    FOREIGN KEY (machineID) REFERENCES machines(machineID)
);
