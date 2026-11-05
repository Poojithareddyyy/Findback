
package Controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Model_Layer.User;
import Service_Layer.UserService;
import Service_Implementation.UserServiceImpl;

public class MemberRegisterServlet extends HttpServlet {

    private UserService service = new UserServiceImpl();

    // CORS configuration for VS Code Live Server
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
            "Content-Type"
        );
    }

    // Handle browser preflight request
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

        // Read form values
        String orgCode = request.getParameter("organizationCode");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Check empty fields
        if (orgCode == null || fullName == null ||
            email == null || password == null ||
            confirmPassword == null ||
            orgCode.trim().isEmpty() ||
            fullName.trim().isEmpty() ||
            email.trim().isEmpty() ||
            password.isEmpty() ||
            confirmPassword.isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Please fill all required fields.\"}"
            );

            return;
        }

        orgCode = orgCode.trim().toUpperCase();
        fullName = fullName.trim();
        email = email.trim();

        // Validate organization code format
        if (!orgCode.matches("FB[A-Z0-9]{6}")) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Invalid organization code format.\"}"
            );

            return;
        }

        // Validate name
        if (!fullName.matches("[a-zA-Z\\s.'-]{2,100}")) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Please enter a valid full name.\"}"
            );

            return;
        }

        // Validate email
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Please enter a valid email.\"}"
            );

            return;
        }

        // Validate password strength
        if (password.length() < 8 ||
            !password.matches(".*[A-Z].*") ||
            !password.matches(".*[a-z].*") ||
            !password.matches(".*[0-9].*") ||
            !password.matches(".*[^A-Za-z0-9].*")) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Password must contain uppercase, lowercase, number and special character.\"}"
            );

            return;
        }

        // Check password confirmation
        if (!password.equals(confirmPassword)) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Passwords do not match.\"}"
            );

            return;
        }

        // Create User object
        User user = new User(
            0,
            fullName,
            email,
            password,
            "MEMBER"
        );

        // Call service
        boolean result = service.registerMember(orgCode, user);

        if (result) {

            response.setStatus(HttpServletResponse.SC_OK);

            response.getWriter().print(
                "{\"success\":true,\"message\":\"Member registered successfully! You can now login.\"}"
            );

        } else {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Registration failed. Check your organization code or email details.\"}"
            );
        }
    }
}
