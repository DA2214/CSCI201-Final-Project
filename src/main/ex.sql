INSERT INTO Users (uid, username, email, password)
VALUES (101, 'nelsonvo', 'nqvo@usc.edu', 'password123');
INSERT INTO Machines (machineID, machineName) VALUES
(1, 'Treadmill 1'),
(2, 'Treadmill 2'),
(3, 'Bench 1');
INSERT INTO MachineUsage (usageID, userID, machineID, date, duration) VALUES
(1, 101, 1, '2025-01-20', 45),
(2, 101, 2, '2025-01-22', 90),
(3, 101, 1, '2025-01-25', 120),
(4, 101, 3, '2025-02-01', 60);
INSERT INTO Reservations (reservationID, userID, machineID, date, startTime, endTime, status) VALUES
(1, 101, 1, '2025-01-22', '10:00', '11:00', 'Completed'),
(2, 101, 2, '2025-01-25', '14:00', '15:00', 'Completed'),
(3, 101, 3, '2025-02-03', '09:00', '10:30', 'Upcoming');
