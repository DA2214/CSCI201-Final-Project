-- ================================================================
-- MACHINE SEED DATA
-- ================================================================
-- This file populates the Machines table with a realistic set of 
-- gym equipment for the USC Village gym.
-- Updated to use 'Cardio' and 'Strength' types for frontend filtering
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
('Treadmill #1', 'Cardio', 'AVAILABLE'),
('Treadmill #2', 'Cardio', 'AVAILABLE'),
('Treadmill #3', 'Cardio', 'AVAILABLE'),
('Treadmill #4', 'Cardio', 'AVAILABLE'),
('Treadmill #5', 'Cardio', 'AVAILABLE'),
('Treadmill #6', 'Cardio', 'AVAILABLE'),
('Treadmill #7', 'Cardio', 'AVAILABLE'),
('Treadmill #8', 'Cardio', 'AVAILABLE');

-- Stairmasters
INSERT INTO Machines (name, type, status) VALUES
('Stairmaster #1', 'Cardio', 'AVAILABLE'),
('Stairmaster #2', 'Cardio', 'AVAILABLE'),
('Stairmaster #3', 'Cardio', 'AVAILABLE');

-- ======================
-- STRENGTH MACHINES
-- ======================

-- Bench Press machines
INSERT INTO Machines (name, type, status) VALUES
('Bench Press #1', 'Strength', 'AVAILABLE'),
('Bench Press #2', 'Strength', 'AVAILABLE'),
('Bench Press #3', 'Strength', 'AVAILABLE'),
('Bench Press #4', 'Strength', 'AVAILABLE');

-- Squat Racks
INSERT INTO Machines (name, type, status) VALUES
('Squat Rack #1', 'Strength', 'AVAILABLE'),
('Squat Rack #2', 'Strength', 'AVAILABLE'),
('Squat Rack #3', 'Strength', 'AVAILABLE');

-- Shoulder Press machines
INSERT INTO Machines (name, type, status) VALUES
('Shoulder Press #1', 'Strength', 'AVAILABLE'),
('Shoulder Press #2', 'Strength', 'AVAILABLE');

-- Leg Press machines
INSERT INTO Machines (name, type, status) VALUES
('Leg Press #1', 'Strength', 'AVAILABLE'),
('Leg Press #2', 'Strength', 'AVAILABLE');

-- Leg Extension machines
INSERT INTO Machines (name, type, status) VALUES
('Leg Extension #1', 'Strength', 'AVAILABLE'),
('Leg Extension #2', 'Strength', 'AVAILABLE');

-- Leg Curl machines
INSERT INTO Machines (name, type, status) VALUES
('Leg Curl #1', 'Strength', 'AVAILABLE'),
('Leg Curl #2', 'Strength', 'AVAILABLE');

-- Lat Pulldown machines
INSERT INTO Machines (name, type, status) VALUES
('Lat Pulldown #1', 'Strength', 'AVAILABLE'),
('Lat Pulldown #2', 'Strength', 'AVAILABLE'),
('Lat Pulldown #3', 'Strength', 'AVAILABLE');

-- Seated Row machines
INSERT INTO Machines (name, type, status) VALUES
('Seated Row #1', 'Strength', 'AVAILABLE'),
('Seated Row #2', 'Strength', 'AVAILABLE'),
('Seated Row #3', 'Strength', 'AVAILABLE');

-- Cable Machines
INSERT INTO Machines (name, type, status) VALUES
('Cable Machine #1', 'Strength', 'AVAILABLE'),
('Cable Machine #2', 'Strength', 'AVAILABLE'),
('Cable Machine #3', 'Strength', 'AVAILABLE'),
('Cable Machine #4', 'Strength', 'AVAILABLE');

-- Verify insertion
SELECT type, COUNT(*) as count FROM Machines GROUP BY type ORDER BY type;
SELECT 'Total Cardio:', COUNT(*) FROM Machines WHERE type='Cardio';
SELECT 'Total Strength:', COUNT(*) FROM Machines WHERE type='Strength';
