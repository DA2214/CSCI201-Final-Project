-- ================================================================
-- RESERVATION SYSTEM DATABASE SCHEMA
-- ================================================================
-- This file adds the Reservations table for the gym machine 
-- reservation system.
-- 
-- NOTE: Machines table is defined in setup.sql
-- This creates the Reservations table for ACTIVE reservations.
-- Completed workouts are automatically pushed to workout_history.
-- ================================================================

USE TrojanTracker;

--------------------------------------------------
-- RESERVATIONS TABLE
--------------------------------------------------
-- Stores machine reservations with 5-minute claim window
-- User has 5 minutes to start workout, then gets full intended_duration
CREATE TABLE IF NOT EXISTS Reservations (
    reservationId INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    machineId INT NOT NULL,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiresAt TIMESTAMP NOT NULL,  -- 5-minute claim window
    status ENUM('ACTIVE', 'EXPIRED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    intendedDuration INT NOT NULL DEFAULT 60,  -- Duration in minutes user wants
    workoutStartTime TIMESTAMP NULL,  -- When user actually starts workout
    workoutEndTime TIMESTAMP NULL,    -- When workout is scheduled to end
    
    FOREIGN KEY (userId) REFERENCES users(uid) ON DELETE CASCADE,
    FOREIGN KEY (machineId) REFERENCES Machines(machineId) ON DELETE CASCADE,
    
    INDEX idx_userId (userId),
    INDEX idx_machineId (machineId),
    INDEX idx_status (status),
    INDEX idx_expiresAt (expiresAt),
    INDEX idx_workoutEndTime (workoutEndTime)
);

