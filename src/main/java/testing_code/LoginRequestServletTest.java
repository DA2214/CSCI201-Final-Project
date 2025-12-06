// Test Case 3
import org.junit.jupiter.api.*;

public class LoginRequestServletTest extends BaseServletTestSupport {

    private LoginRequestServlet servlet;

    @BeforeEach
    void setUpLogin() throws Exception {
        super.baseSetUp();
        servlet = new LoginRequestServlet();
        // Seed DB once with user: testuser / password: testpass
        Connection conn = DatabaseAccessor.GetDatabaseConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Users(username, passwordHash) VALUES (?, ?)")) {
            ps.setString(1, "testuser");
            ps.setString(2, "testpass"); // or hashed if your code hashes
            ps.executeUpdate();
        }
    }

    @Test
    void login_usernameExists_wrongPassword() throws Exception {
        when(request.getParameter("username")).thenReturn("testuser");
        when(request.getParameter("password")).thenReturn("wrong");

        servlet.doPost(request, response);

        String json = responseWriter.toString();
        assertTrue(json.contains("success"));
        assertTrue(json.contains("false"));
    }

    @Test
    void login_usernameExists_correctPassword() throws Exception {
        when(request.getParameter("username")).thenReturn("testuser");
        when(request.getParameter("password")).thenReturn("testpass");

        servlet.doPost(request, response);

        String json = responseWriter.toString();
        assertTrue(json.contains("success"));
        assertTrue(json.contains("true"));
    }

    @Test
    void login_usernameDoesNotExist() throws Exception {
        when(request.getParameter("username")).thenReturn("nouser");
        when(request.getParameter("password")).thenReturn("whatever");

        servlet.doPost(request, response);

        String json = responseWriter.toString();
        assertTrue(json.contains("success"));
        assertTrue(json.contains("false"));
    }

    @Test
    void login_sqlInjectionAttempt_fails() throws Exception {
        when(request.getParameter("username")).thenReturn("admin' OR 1=1 --");
        when(request.getParameter("password")).thenReturn("anything");

        servlet.doPost(request, response);

        String json = responseWriter.toString();
        assertTrue(json.contains("success"));
        assertTrue(json.contains("false"));
    }
}
