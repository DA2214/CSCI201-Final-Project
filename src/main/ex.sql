USE GymSync;

-- Create test user (auto assigned uid)
INSERT INTO users (username, email, password)
VALUES ('nelsonvo', 'nqvo@usc.edu', 'password123');

-- Machine data comes from machine_seed_data.sql (not here)

-- Insert past workout history for analytics
INSERT INTO workout_history (userID, machineID, startTime, endTime, status, date) VALUES
(1, 'Treadmill #1', '10:00:00', '11:00:00', 'Completed', '2025-01-22'),
(1, 'Treadmill #2', '14:00:00', '15:00:00', 'Completed', '2025-01-25'),
(1, 'Bench Press #1', '09:00:00', '10:30:00', 'Completed', '2025-02-03');

-- Verify data inserted
SELECT 'Test user created:', username FROM users WHERE username='nelsonvo';
SELECT 'Machine usage records:', COUNT(*) FROM machineusage WHERE userID=1;
SELECT 'Workout history records:', COUNT(*) FROM workout_history WHERE userID=1;
