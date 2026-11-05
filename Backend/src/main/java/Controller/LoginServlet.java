
package Controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Model_Layer.User;
import Service_Layer.LoginService;
import Service_Implementation.EmailService;
import Service_Implementation.LoginServiceImpl;
import Data_Layer.SessionDAO;
import Data_Layer.LoginDAO;

public class LoginServlet extends HttpServlet {

    private LoginService service = new LoginServiceImpl();
    private SessionDAO sessionDAO = new SessionDAO();
    private LoginDAO loginDAO = new LoginDAO();

    // CORS configuration
    private void setCorsHeaders(HttpServletResponse response) {

        response.setHeader(
            "Access-Control-Allow-Origin",
            "http://127.0.0.1:5500"
        );

        response.setHeader(
            "Access-Control-Allow-Methods",
            "POST, OPTIONS"
        );

        response.setHeader(
            "Access-Control-Allow-Headers",
            "Content-Type, Authorization"
        );
    }

    // Handle preflight request
    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response)
                             throws ServletException, IOException {

        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        setCorsHeaders(response);

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String orgCode = request.getParameter("organizationCode");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Check empty fields
        if (orgCode == null || email == null || password == null ||
            orgCode.trim().isEmpty() ||
            email.trim().isEmpty() ||
            password.isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Please enter all login details.\"}"
            );

            return;
        }

        orgCode = orgCode.trim().toUpperCase();
        email = email.trim();

        // Authenticate user
        User user = service.login(orgCode, email, password);

        if (user == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Invalid organization code, email or password.\"}"
            );

            return;
        }

        try {

            // Generate secure random session token
            byte[] randomBytes = new byte[32];
            new SecureRandom().nextBytes(randomBytes);

            String token = Base64.getUrlEncoder()
                                 .withoutPadding()
                                 .encodeToString(randomBytes);

            // Hash token before storing in database
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                token.getBytes("UTF-8")
            );

            StringBuilder tokenHash = new StringBuilder();

            for (byte b : hashBytes) {
                tokenHash.append(String.format("%02x", b));
            }

            // Session expires after 1 hour
            Timestamp expiresAt = new Timestamp(
                System.currentTimeMillis() + (60 * 60 * 1000)
            );

            // Save session in database
            boolean saved = sessionDAO.saveSession(
                user.getUserId(),
                user.getOrganizationId(),
                tokenHash.toString(),
                expiresAt
            );
            

            if (!saved) {

                response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                );

                response.getWriter().print(
                    "{\"success\":false,\"message\":\"Unable to create login session.\"}"
                );

                return;
            }
            
         // Send acknowledgment only on first successful login
            if (loginDAO.markFirstLogin(user.getUserId())) {
                EmailService.sendLoginAcknowledgment(user);
            }

            // Determine dashboard based on role
            String dashboard;

            if ("ORG_ADMIN".equals(user.getRole())) {
                dashboard = "AdminDashboard.html";
            } else {
                dashboard = "UserDashboard.html";
            }

            response.setStatus(HttpServletResponse.SC_OK);

            response.getWriter().print(
                "{\"success\":true," +
                "\"message\":\"Login successful!\"," +
                "\"token\":\"" + token + "\"," +
                "\"role\":\"" + user.getRole() + "\"," +
                "\"fullName\":\"" + user.getFullName() + "\","+
                "\"dashboard\":\"" + dashboard + "\"  }"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Something went wrong during login.\"}"
            );
        }
    }
}
