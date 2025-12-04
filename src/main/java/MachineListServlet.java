
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Machine List Servlet
 * 
 * Returns all gym machines with current status for display
 * Automatically expires old reservations before returning data
 */
@WebServlet("/machines")
public class MachineListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * GET /machines - List all gym machines with their current status
     * 
     * Returns JSON array of machines:
     * [
     *   {
     *     "machineId": 1,
     *     "name": "Treadmill #1",
     *     "type": "cardio",
     *     "status": "AVAILABLE"
     *   },
     *   ...
     * ]
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // Expire old reservations before listing machines
            ReservationService.expireOldReservations();

            Connection conn = DatabaseAccessor.GetDatabaseConnection();
            
            // Get all machines
            String query = "SELECT machineId, name, type, status FROM Machines ORDER BY type, name";
            List<Machine> machines = new ArrayList<>();
            
            try (PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Machine machine = new Machine();
                    machine.setMachineId(rs.getInt("machineId"));
                    machine.setName(rs.getString("name"));
                    machine.setType(rs.getString("type"));
                    machine.setStatus(MachineStatus.valueOf(rs.getString("status")));
                    machines.add(machine);
                }
            }

            // Return as JSON
            String json = gson.toJson(machines);
            resp.getWriter().write(json);

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(new ErrorResponse("Error fetching machines: " + e.getMessage())));
        }
    }

    // Helper class for error responses
    class ErrorResponse {
        String error;
        
        ErrorResponse(String error) {
            this.error = error;
        }
    }
}
