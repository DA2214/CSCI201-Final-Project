

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

    // ---------- POJO CLASSES ----------
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
        responseObj.reservations = new ArrayList<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DatabaseAccessor.GetDatabaseConnection()) {

                // -----------------------------
                // WORKOUT HISTORY QUERY
                // -----------------------------
                // Updated to use new Machines table
                String reservationQuery =
                        "SELECT r.reservationId, r.startTime, r.endTime, r.status, r.date, " +
                        "       CAST(m.machineId AS CHAR) AS machineID, m.name AS machineName " +
                        "FROM workout_history r " +
                        "JOIN Machines m ON r.machineID = m.name " +
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
