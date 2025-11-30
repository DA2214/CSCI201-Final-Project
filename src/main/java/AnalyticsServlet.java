import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet("/AnalyticsServlet")
public class AnalyticsServlet extends HttpServlet {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/TrojanTracker";
    private static final String JDBC_USER = "root";   // change if needed
    private static final String JDBC_PASS = "root";   // change if needed

    // ---------- POJO CLASSES ----------
    class MachineUsageRecord {
        int usageID;
        int duration;
        String date;
        String machineID;
        String machineName;
    }

    class ReservationRecord {
        int reservationId;
        String startTime;
        String endTime;
        String status;
        String date;
        String machineID;
        String machineName;
    }

    class UserHistoryResponse {
        List<MachineUsageRecord> machineUsage;
        List<ReservationRecord> reservations;
    }
    // -----------------------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String userIdParam = req.getParameter("userID");

        if (userIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing userID parameter");
            return;
        }

        int userID = Integer.parseInt(userIdParam);

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        UserHistoryResponse responseObj = new UserHistoryResponse();
        responseObj.machineUsage = new ArrayList<>();
        responseObj.reservations = new ArrayList<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS)) {

                // -----------------------------
                // MACHINE USAGE QUERY
                // -----------------------------
                String usageQuery =
                        "SELECT mu.usageID, mu.duration, mu.date, " +
                        "       m.machineID, m.machineName " +
                        "FROM machineusage mu " +
                        "JOIN machines m ON mu.machineID = m.machineID " +
                        "WHERE mu.userID = ?";

                PreparedStatement usageStmt = conn.prepareStatement(usageQuery);
                usageStmt.setInt(1, userID);
                ResultSet usageRs = usageStmt.executeQuery();

                while (usageRs.next()) {
                    MachineUsageRecord record = new MachineUsageRecord();
                    record.usageID = usageRs.getInt("usageID");
                    record.duration = usageRs.getInt("duration");
                    record.date = usageRs.getString("date");
                    record.machineID = usageRs.getString("machineID");
                    record.machineName = usageRs.getString("machineName");
                    responseObj.machineUsage.add(record);
                }

                // -----------------------------
                // RESERVATION QUERY
                // -----------------------------
                String reservationQuery =
                        "SELECT r.reservationId, r.startTime, r.endTime, r.status, r.date, " +
                        "       m.machineID, m.machineName " +
                        "FROM reservations r " +
                        "JOIN machines m ON r.machineID = m.machineID " +
                        "WHERE r.userID = ?";

                PreparedStatement resStmt = conn.prepareStatement(reservationQuery);
                resStmt.setInt(1, userID);
                ResultSet resRs = resStmt.executeQuery();

                while (resRs.next()) {
                    ReservationRecord record = new ReservationRecord();
                    record.reservationId = resRs.getInt("reservationId");
                    record.startTime = resRs.getString("startTime");
                    record.endTime = resRs.getString("endTime");
                    record.status = resRs.getString("status");
                    record.date = resRs.getString("date");
                    record.machineID = resRs.getString("machineID");
                    record.machineName = resRs.getString("machineName");
                    responseObj.reservations.add(record);
                }

                // -----------------------------
                // Convert to JSON with GSON
                // -----------------------------
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(responseObj);

                out.print(json);
                out.flush();

            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server Error: " + e.getMessage());
        }
    }
}
