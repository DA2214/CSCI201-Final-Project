-- ================================================================
-- MACHINE SEED DATA
-- ================================================================
-- This file populates the Machines table with a realistic set of 
-- gym equipment for the USC Village gym.
-- Updated to use 'cardio' and 'strength' types for frontend filtering
-- ================================================================

USE TrojanTracker;

-- Clear existing machines if needed (comment out to preserve existing data)
-- TRUNCATE TABLE Machines;

-- Insert gym machines
-- Format: (name, type, status)

-- ======================
-- CARDIO MACHINES
-- ======================

-- Treadmills
INSERT INTO Machines (name, type, status) VALUES
('Treadmill #1', 'cardio', 'AVAILABLE'),
('Treadmill #2', 'cardio', 'AVAILABLE'),
('Treadmill #3', 'cardio', 'AVAILABLE'),
('Treadmill #4', 'cardio', 'AVAILABLE'),
('Treadmill #5', 'cardio', 'AVAILABLE'),
('Treadmill #6', 'cardio', 'AVAILABLE'),
('Treadmill #7', 'cardio', 'AVAILABLE'),
('Treadmill #8', 'cardio', 'AVAILABLE');

-- Stairmasters
INSERT INTO Machines (name, type, status) VALUES
('Stairmaster #1', 'cardio', 'AVAILABLE'),
('Stairmaster #2', 'cardio', 'AVAILABLE'),
('Stairmaster #3', 'cardio', 'AVAILABLE');

-- ======================
-- STRENGTH MACHINES
-- ======================

-- Bench Press machines
INSERT INTO Machines (name, type, status) VALUES
('Bench Press #1', 'strength', 'AVAILABLE'),
('Bench Press #2', 'strength', 'AVAILABLE'),
('Bench Press #3', 'strength', 'AVAILABLE'),
('Bench Press #4', 'strength', 'AVAILABLE');

-- Squat Racks
INSERT INTO Machines (name, type, status) VALUES
('Squat Rack #1', 'strength', 'AVAILABLE'),
('Squat Rack #2', 'strength', 'AVAILABLE'),
('Squat Rack #3', 'strength', 'AVAILABLE');

-- Shoulder Press machines
INSERT INTO Machines (name, type, status) VALUES
('Shoulder Press #1', 'strength', 'AVAILABLE'),
('Shoulder Press #2', 'strength', 'AVAILABLE');

-- Leg Press machines
INSERT INTO Machines (name, type, status) VALUES
('Leg Press #1', 'strength', 'AVAILABLE'),
('Leg Press #2', 'strength', 'AVAILABLE');

-- Leg Extension machines
INSERT INTO Machines (name, type, status) VALUES
('Leg Extension #1', 'strength', 'AVAILABLE'),
('Leg Extension #2', 'strength', 'AVAILABLE');

-- Leg Curl machines
INSERT INTO Machines (name, type, status) VALUES
('Leg Curl #1', 'strength', 'AVAILABLE'),
('Leg Curl #2', 'strength', 'AVAILABLE');

-- Lat Pulldown machines
INSERT INTO Machines (name, type, status) VALUES
('Lat Pulldown #1', 'strength', 'AVAILABLE'),
('Lat Pulldown #2', 'strength', 'AVAILABLE'),
('Lat Pulldown #3', 'strength', 'AVAILABLE');

-- Seated Row machines
INSERT INTO Machines (name, type, status) VALUES
('Seated Row #1', 'strength', 'AVAILABLE'),
('Seated Row #2', 'strength', 'AVAILABLE'),
('Seated Row #3', 'strength', 'AVAILABLE');

-- Cable Machines
INSERT INTO Machines (name, type, status) VALUES
('Cable Machine #1', 'strength', 'AVAILABLE'),
('Cable Machine #2', 'strength', 'AVAILABLE'),
('Cable Machine #3', 'strength', 'AVAILABLE'),
('Cable Machine #4', 'strength', 'AVAILABLE');

-- Verify insertion
SELECT type, COUNT(*) as count FROM Machines GROUP BY type ORDER BY type;
SELECT 'Total Cardio:', COUNT(*) FROM Machines WHERE type='cardio';
SELECT 'Total Strength:', COUNT(*) FROM Machines WHERE type='strength';
