
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Reservation Logic Service
 * 
 * Coordinates reservation operations and database interactions:
 * - Creating reservations with 5-minute claim windows
 * - Starting workouts and managing duration timers
 * - Canceling reservations before workout starts
 * - Auto-expiring unclaimed reservations
 * - Auto-releasing completed workouts
 * - Pushing completed workouts to analytics
 */
public class ReservationService {

    // 5 minutes claim window in milliseconds
    private static final long CLAIM_WINDOW_MS = 5 * 60 * 1000;

    /**
     * Expire unclaimed reservations and release completed workouts
     * Called automatically at the start of each reservation operation
     */
    public static void expireOldReservations() {
        try {
            Connection conn = DatabaseAccessor.GetDatabaseConnection();
            Timestamp now = new Timestamp(System.currentTimeMillis());
            
            // Find and expire unclaimed reservations (claim window passed)
            String expireQuery = "SELECT reservationId, machineId FROM Reservations " +
                                "WHERE status = ? AND expiresAt < ? AND workoutStartTime IS NULL";
            List<Integer> expiredReservationIds = new ArrayList<>();
            List<Integer> expiredMachineIds = new ArrayList<>();
            
            try (PreparedStatement expirePs = conn.prepareStatement(expireQuery)) {
                expirePs.setString(1, ReservationStatus.ACTIVE.name());
                expirePs.setTimestamp(2, now);
                try (ResultSet expireRs = expirePs.executeQuery()) {
                    while (expireRs.next()) {
                        expiredReservationIds.add(expireRs.getInt("reservationId"));
                        expiredMachineIds.add(expireRs.getInt("machineId"));
                    }
                }
            }
            
            // Mark expired reservations and free machines
            for (int i = 0; i < expiredReservationIds.size(); i++) {
                String updateReservation = "UPDATE Reservations SET status = ? WHERE reservationId = ?";
                try (PreparedStatement updatePs = conn.prepareStatement(updateReservation)) {
                    updatePs.setString(1, ReservationStatus.EXPIRED.name());
                    updatePs.setInt(2, expiredReservationIds.get(i));
                    updatePs.executeUpdate();
                }
                
                // Set machine back to available
                String updateMachine = "UPDATE Machines SET status = ? WHERE machineId = ?";
                try (PreparedStatement machinePs = conn.prepareStatement(updateMachine)) {
                    machinePs.setString(1, MachineStatus.AVAILABLE.name());
                    machinePs.setInt(2, expiredMachineIds.get(i));
                    machinePs.executeUpdate();
                }
            }
            
            // Release machines where workout duration has ended
            String completeQuery = "SELECT reservationId, userId, machineId, workoutStartTime, workoutEndTime " +
                                  "FROM Reservations " +
                                  "WHERE status = ? AND workoutEndTime IS NOT NULL AND workoutEndTime < ?";
            List<Integer> completedReservationIds = new ArrayList<>();
            List<Integer> completedUserIds = new ArrayList<>();
            List<Integer> completedMachineIds = new ArrayList<>();
            List<Timestamp> completedStartTimes = new ArrayList<>();
            List<Timestamp> completedEndTimes = new ArrayList<>();
            
            try (PreparedStatement completePs = conn.prepareStatement(completeQuery)) {
                completePs.setString(1, ReservationStatus.ACTIVE.name());
                completePs.setTimestamp(2, now);
                try (ResultSet completeRs = completePs.executeQuery()) {
                    while (completeRs.next()) {
                        completedReservationIds.add(completeRs.getInt("reservationId"));
                        completedUserIds.add(completeRs.getInt("userId"));
                        completedMachineIds.add(completeRs.getInt("machineId"));
                        completedStartTimes.add(completeRs.getTimestamp("workoutStartTime"));
                        completedEndTimes.add(completeRs.getTimestamp("workoutEndTime"));
                    }
                }
            }
            
            // Process completed workouts
            for (int i = 0; i < completedReservationIds.size(); i++) {
                // Push to workout_history table for analytics
                pushToAnalyticsTable(conn, completedUserIds.get(i), completedMachineIds.get(i), 
                                    completedStartTimes.get(i), completedEndTimes.get(i));
                
                // Mark as completed in new table
                String updateReservation = "UPDATE Reservations SET status = ? WHERE reservationId = ?";
                try (PreparedStatement updatePs = conn.prepareStatement(updateReservation)) {
                    updatePs.setString(1, ReservationStatus.COMPLETED.name());
                    updatePs.setInt(2, completedReservationIds.get(i));
                    updatePs.executeUpdate();
                }
                
                // Set machine back to available
                String updateMachine = "UPDATE Machines SET status = ? WHERE machineId = ?";
                try (PreparedStatement machinePs = conn.prepareStatement(updateMachine)) {
                    machinePs.setString(1, MachineStatus.AVAILABLE.name());
                    machinePs.setInt(2, completedMachineIds.get(i));
                    machinePs.executeUpdate();
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Push completed workout to workout_history table for analytics integration
     * Converts timestamp format and machine ID type for compatibility
     */
    private static void pushToAnalyticsTable(Connection conn, int userId, int machineId, 
                                            Timestamp startTime, Timestamp endTime) {
        try {
            // Get machine name for workout_history compatibility
            String getMachineName = "SELECT name FROM Machines WHERE machineId = ?";
            String machineIDStr = String.valueOf(machineId); // Default to machineId as string
            
            try (PreparedStatement getMachinePs = conn.prepareStatement(getMachineName)) {
                getMachinePs.setInt(1, machineId);
                try (ResultSet machineRs = getMachinePs.executeQuery()) {
                    if (machineRs.next()) {
                        machineIDStr = machineRs.getString("name"); // Use machine name as machineID
                    }
                }
            }
            
            // Insert workout record for analytics
            String insertAnalytics = "INSERT INTO workout_history (userID, machineID, startTime, endTime, status, date) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insertPs = conn.prepareStatement(insertAnalytics)) {
                insertPs.setInt(1, userId);
                insertPs.setString(2, machineIDStr);
                insertPs.setTime(3, new java.sql.Time(startTime.getTime())); // Convert TIMESTAMP to TIME
                insertPs.setTime(4, new java.sql.Time(endTime.getTime())); // Convert TIMESTAMP to TIME
                insertPs.setString(5, "Completed");
                insertPs.setDate(6, new java.sql.Date(startTime.getTime())); // Convert TIMESTAMP to DATE
                insertPs.executeUpdate();
            }
            
        } catch (SQLException e) {
            // Log but don't fail the whole operation if analytics push fails
            System.err.println("Warning: Failed to push workout to workout_history table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a new reservation with 5-minute claim window
     * User must start workout within 5 minutes or reservation expires
     * 
     * @param userId User's ID
     * @param machineId Machine's ID
     * @param durationMinutes Intended workout duration (15-120 minutes)
     * @return Reservation ID if successful, -1 if failed
     */
    public static int createReservation(int userId, int machineId, int durationMinutes) {
        try {
            Connection conn = DatabaseAccessor.GetDatabaseConnection();
            
            // First expire old reservations and release completed workouts
            expireOldReservations();
            
            // Check if machine exists and is available
            String checkMachine = "SELECT status FROM Machines WHERE machineId = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkMachine)) {
                checkPs.setInt(1, machineId);
                try (ResultSet machineRs = checkPs.executeQuery()) {
                    if (!machineRs.next()) {
                        return -1; // Machine doesn't exist
                    }
                    
                    String status = machineRs.getString("status");
                    if (!MachineStatus.AVAILABLE.name().equals(status)) {
                        return -1; // Machine not available
                    }
                }
            }
            
            // Check if there's already an active reservation for this machine
            String checkReservation = "SELECT reservationId FROM Reservations " +
                                     "WHERE machineId = ? AND status = ? LIMIT 1";
            try (PreparedStatement checkResPs = conn.prepareStatement(checkReservation)) {
                checkResPs.setInt(1, machineId);
                checkResPs.setString(2, ReservationStatus.ACTIVE.name());
                try (ResultSet resRs = checkResPs.executeQuery()) {
                    if (resRs.next()) {
                        return -1; // Machine already reserved
                    }
                }
            }
            
            // Create timestamps - 5 minute claim window
            Timestamp createdAt = new Timestamp(System.currentTimeMillis());
            Timestamp expiresAt = new Timestamp(createdAt.getTime() + CLAIM_WINDOW_MS);
            
            // Create reservation with intended duration
            String insertReservation = "INSERT INTO Reservations (userId, machineId, createdAt, expiresAt, " +
                                      "status, intendedDuration) VALUES (?, ?, ?, ?, ?, ?)";
            int reservationId = -1;
            
            try (PreparedStatement insertPs = conn.prepareStatement(insertReservation, Statement.RETURN_GENERATED_KEYS)) {
                insertPs.setInt(1, userId);
                insertPs.setInt(2, machineId);
                insertPs.setTimestamp(3, createdAt);
                insertPs.setTimestamp(4, expiresAt);
                insertPs.setString(5, ReservationStatus.ACTIVE.name());
                insertPs.setInt(6, durationMinutes);
                insertPs.executeUpdate();
                
                try (ResultSet generatedKeys = insertPs.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reservationId = generatedKeys.getInt(1);
                    }
                }
            }
            
            if (reservationId > 0) {
                // Update machine status to reserved
                String updateMachine = "UPDATE Machines SET status = ? WHERE machineId = ?";
                try (PreparedStatement updatePs = conn.prepareStatement(updateMachine)) {
                    updatePs.setString(1, MachineStatus.RESERVED.name());
                    updatePs.setInt(2, machineId);
                    updatePs.executeUpdate();
                }
            }
            
            return reservationId;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Start using a reserved machine
     * Sets workout timer based on intended duration
     * 
     * @param userId User's ID
     * @param machineId Machine's ID
     * @return true if successful, false if no reservation or already started
     */
    public static boolean startMachine(int userId, int machineId) {
        try {
            Connection conn = DatabaseAccessor.GetDatabaseConnection();
            
            // Expire old reservations and release completed workouts
            expireOldReservations();
            
            // Get the reservation for this user and machine
            String getReservation = "SELECT reservationId, expiresAt, intendedDuration, workoutStartTime " +
                                   "FROM Reservations WHERE userId = ? AND machineId = ? AND status = ? LIMIT 1";
            int reservationId;
            Timestamp expiresAt;
            int intendedDuration;
            
            try (PreparedStatement getPs = conn.prepareStatement(getReservation)) {
                getPs.setInt(1, userId);
                getPs.setInt(2, machineId);
                getPs.setString(3, ReservationStatus.ACTIVE.name());
                try (ResultSet rs = getPs.executeQuery()) {
                    if (!rs.next()) {
                        return false; // No active reservation found
                    }
                    
                    reservationId = rs.getInt("reservationId");
                    expiresAt = rs.getTimestamp("expiresAt");
                    intendedDuration = rs.getInt("intendedDuration");
                    Timestamp existingStartTime = rs.getTimestamp("workoutStartTime");
                    
                    // Prevent restarting an active workout
                    if (existingStartTime != null) {
                        return false; // Workout already started - cannot restart
                    }
                    
                    // Check if reservation claim window hasn't expired
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    if (now.after(expiresAt)) {
                        return false; // Claim window expired
                    }
                }
            }
            
            // Calculate workout end time based on intended duration
            long durationMs = intendedDuration * 60 * 1000L;
            Timestamp workoutStartTime = new Timestamp(System.currentTimeMillis());
            Timestamp workoutEndTime = new Timestamp(workoutStartTime.getTime() + durationMs);
            
            // Update reservation with workout times
            String updateReservation = "UPDATE Reservations SET workoutStartTime = ?, workoutEndTime = ? " +
                                      "WHERE reservationId = ?";
            try (PreparedStatement updatePs = conn.prepareStatement(updateReservation)) {
                updatePs.setTimestamp(1, workoutStartTime);
                updatePs.setTimestamp(2, workoutEndTime);
                updatePs.setInt(3, reservationId);
                updatePs.executeUpdate();
            }
            
            // Update machine status to in use
            String updateMachine = "UPDATE Machines SET status = ? WHERE machineId = ?";
            try (PreparedStatement machinePs = conn.prepareStatement(updateMachine)) {
                machinePs.setString(1, MachineStatus.IN_USE.name());
                machinePs.setInt(2, machineId);
                machinePs.executeUpdate();
            }
            
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cancel a reservation (only allowed before workout starts)
     * 
     * @param userId User's ID
     * @param machineId Machine's ID  
     * @return true if successful, false if no reservation or already started
     */
    public static boolean cancelReservation(int userId, int machineId) {
        try {
            Connection conn = DatabaseAccessor.GetDatabaseConnection();
            
            // Expire old reservations and release completed workouts
            expireOldReservations();
            
            // Get the reservation for this user and machine
            String getReservation = "SELECT reservationId, workoutStartTime FROM Reservations " +
                                   "WHERE userId = ? AND machineId = ? AND status = ? LIMIT 1";
            int reservationId;
            
            try (PreparedStatement getPs = conn.prepareStatement(getReservation)) {
                getPs.setInt(1, userId);
                getPs.setInt(2, machineId);
                getPs.setString(3, ReservationStatus.ACTIVE.name());
                try (ResultSet rs = getPs.executeQuery()) {
                    if (!rs.next()) {
                        return false; // No active reservation found
                    }
                    
                    reservationId = rs.getInt("reservationId");
                    Timestamp workoutStartTime = rs.getTimestamp("workoutStartTime");
                    
                    // Cannot cancel after workout has started
                    if (workoutStartTime != null) {
                        return false; // Cannot cancel after workout has started
                    }
                }
            }
            
            // Mark reservation as cancelled
            String updateReservation = "UPDATE Reservations SET status = ? WHERE reservationId = ?";
            try (PreparedStatement updatePs = conn.prepareStatement(updateReservation)) {
                updatePs.setString(1, ReservationStatus.CANCELLED.name());
                updatePs.setInt(2, reservationId);
                updatePs.executeUpdate();
            }
            
            // Set machine back to available
            String updateMachine = "UPDATE Machines SET status = ? WHERE machineId = ?";
            try (PreparedStatement machinePs = conn.prepareStatement(updateMachine)) {
                machinePs.setString(1, MachineStatus.AVAILABLE.name());
                machinePs.setInt(2, machineId);
                machinePs.executeUpdate();
            }
            
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get time remaining in seconds for claim window or active workout
     * 
     * @param userId User's ID
     * @param machineId Machine's ID
     * @return Seconds remaining, or -1 if no active reservation
     */
    public static long getClaimTimeRemaining(int userId, int machineId) {
        try {
            Connection conn = DatabaseAccessor.GetDatabaseConnection();
            
            String getReservation = "SELECT expiresAt, workoutStartTime, workoutEndTime " +
                                   "FROM Reservations WHERE userId = ? AND machineId = ? AND status = ? LIMIT 1";
            try (PreparedStatement getPs = conn.prepareStatement(getReservation)) {
                getPs.setInt(1, userId);
                getPs.setInt(2, machineId);
                getPs.setString(3, ReservationStatus.ACTIVE.name());
                try (ResultSet rs = getPs.executeQuery()) {
                    if (!rs.next()) {
                        return -1; // No active reservation found
                    }
                    
                    Timestamp workoutStartTime = rs.getTimestamp("workoutStartTime");
                    Timestamp workoutEndTime = rs.getTimestamp("workoutEndTime");
                    Timestamp expiresAt = rs.getTimestamp("expiresAt");
                    
                    // If workout already started, return workout time remaining
                    if (workoutStartTime != null && workoutEndTime != null) {
                        Timestamp now = new Timestamp(System.currentTimeMillis());
                        long remaining = workoutEndTime.getTime() - now.getTime();
                        return remaining > 0 ? remaining / 1000 : 0;
                    }
                    
                    // Otherwise return claim window time remaining
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    long remaining = expiresAt.getTime() - now.getTime();
                    
                    return remaining > 0 ? remaining / 1000 : 0;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
