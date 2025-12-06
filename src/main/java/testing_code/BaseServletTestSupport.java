// Helper Setup
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

public class BaseServletTestSupport {

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected StringWriter responseWriter;

    @BeforeEach
    void baseSetUp() throws Exception {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }
}
