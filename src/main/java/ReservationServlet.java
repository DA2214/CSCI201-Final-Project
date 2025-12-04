
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Reservation System Servlet
 * 
 * Handles gym machine reservations with hybrid booking model:
 * 1. User reserves machine + selects duration (15-120 min)
 * 2. User has 5-minute claim window to arrive and start workout
 * 3. Once started, user gets full workout duration
 * 4. Auto-expires if not started within 5 minutes
 * 5. Auto-releases when workout duration completes
 * 
 * Endpoints:
 * - POST /reservation with action="reserve" - Create reservation
 * - POST /reservation with action="start" - Start workout
 * - POST /reservation with action="cancel" - Cancel reservation (before start only)
 */
@WebServlet("/reservation")
public class ReservationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * POST /reservation - Handle reservation operations
     * 
     * Request Body (JSON):
     * - action: "reserve" | "start" | "cancel"
     * - userId: User ID (integer)
     * - machineId: Machine ID (integer)
     * - duration: Workout duration in minutes (15-120, required for "reserve" action only)
     * 
     * Returns JSON response with success status and relevant data
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // Parse request body as JSON
            String body = req.getReader().lines().reduce("", (a, b) -> a + b);
            Type mapType = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> data = gson.fromJson(body, mapType);

            String action = data.get("action");
            
            if (action == null || action.isEmpty()) {
                sendError(resp, "Missing 'action' parameter");
                return;
            }

            // Get userId from request data
            String userIdStr = data.get("userId");
            if (userIdStr == null || userIdStr.isEmpty()) {
                sendError(resp, "Missing 'userId' parameter");
                return;
            }
            int userId = Integer.parseInt(userIdStr);

            // Get machineId
            String machineIdStr = data.get("machineId");
            if (machineIdStr == null || machineIdStr.isEmpty()) {
                sendError(resp, "Missing 'machineId' parameter");
                return;
            }
            int machineId = Integer.parseInt(machineIdStr);

            // Route to appropriate handler
            switch (action) {
                case "reserve":
                    // Get duration (default to 60 minutes if not provided)
                    String durationStr = data.get("duration");
                    int duration = 60; // default
                    if (durationStr != null && !durationStr.isEmpty()) {
                        duration = Integer.parseInt(durationStr);
                    }
                    
                    // Validate duration range
                    if (duration < 15 || duration > 120) {
                        sendError(resp, "Duration must be between 15 and 120 minutes");
                        return;
                    }
                    
                    handleReserve(resp, userId, machineId, duration);
                    break;
                case "start":
                    handleStart(resp, userId, machineId);
                    break;
                case "cancel":
                    handleCancel(resp, userId, machineId);
                    break;
                default:
                    sendError(resp, "Invalid action: " + action);
            }

        } catch (NumberFormatException e) {
            sendError(resp, "Invalid user ID or machine ID format");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Server error: " + e.getMessage());
        }
    }

    /**
     * Reserve Action - Creates a new reservation with 5-minute claim window
     * User must call "start" within 5 minutes or reservation expires
     */
    private void handleReserve(HttpServletResponse resp, int userId, int machineId, int duration)
            throws IOException {

        int reservationId = ReservationService.createReservation(userId, machineId, duration);

        if (reservationId > 0) {
            // Get reservation details
            long claimTimeRemaining = ReservationService.getClaimTimeRemaining(userId, machineId);
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + (claimTimeRemaining * 1000));

            ReservationResponse response = new ReservationResponse(
                true,
                "Machine reserved successfully. You have 5 minutes to start your workout.",
                reservationId,
                machineId,
                expiresAt.toString(),
                (int) claimTimeRemaining,
                duration
            );
            resp.getWriter().write(gson.toJson(response));
        } else {
            sendError(resp, "Failed to reserve machine. Machine may not be available or does not exist.");
        }
    }

    /**
     * Start Action - Begin workout on reserved machine
     * Validates claim window hasn't expired and sets workout timer
     */
    private void handleStart(HttpServletResponse resp, int userId, int machineId)
            throws IOException {

        boolean success = ReservationService.startMachine(userId, machineId);

        if (success) {
            SimpleResponse response = new SimpleResponse(true, "Machine started successfully");
            resp.getWriter().write(gson.toJson(response));
        } else {
            sendError(resp, "Failed to start machine. No active reservation found or reservation expired.");
        }
    }

    /**
     * Cancel Action - Cancel reservation before workout starts
     * Cannot cancel after workout has begun
     */
    private void handleCancel(HttpServletResponse resp, int userId, int machineId)
            throws IOException {

        boolean success = ReservationService.cancelReservation(userId, machineId);

        if (success) {
            SimpleResponse response = new SimpleResponse(true, "Reservation cancelled successfully");
            resp.getWriter().write(gson.toJson(response));
        } else {
            sendError(resp, "Failed to cancel reservation. No active reservation found or workout already started.");
        }
    }

    /**
     * Send error response
     */
    private void sendError(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        SimpleResponse response = new SimpleResponse(false, message);
        resp.getWriter().write(gson.toJson(response));
    }

    // Response classes
    class SimpleResponse {
        boolean success;
        String message;

        SimpleResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    class ReservationResponse {
        boolean success;
        String message;
        int reservationId;
        int machineId;
        String expiresAt;
        int claimTimeRemainingSeconds;
        int workoutDurationMinutes;

        ReservationResponse(boolean success, String message, int reservationId, 
                          int machineId, String expiresAt, int claimTimeRemainingSeconds, 
                          int workoutDurationMinutes) {
            this.success = success;
            this.message = message;
            this.reservationId = reservationId;
            this.machineId = machineId;
            this.expiresAt = expiresAt;
            this.claimTimeRemainingSeconds = claimTimeRemainingSeconds;
            this.workoutDurationMinutes = workoutDurationMinutes;
        }
    }
}

