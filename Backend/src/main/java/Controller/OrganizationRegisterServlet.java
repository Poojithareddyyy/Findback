
package Controller;

import java.io.IOException;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Model_Layer.Organization;
import Model_Layer.User;
import Service_Layer.OrganizationService;
import Service_Implementation.OrganizationServiceImpl;

public class OrganizationRegisterServlet extends HttpServlet {

    private OrganizationService service = new OrganizationServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {
    	
    	setCorsHeaders(response);
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String orgName = request.getParameter("organizationName");
        String orgType = request.getParameter("organizationType");
        String orgEmail = request.getParameter("organizationEmail");
        String phone = request.getParameter("organizationPhone");

        String adminName = request.getParameter("adminName");
        String adminEmail = request.getParameter("adminEmail");
        String password = request.getParameter("adminPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validate required fields
        if (orgName == null || orgType == null ||
            orgEmail == null || adminName == null ||
            adminEmail == null || password == null ||
            confirmPassword == null ||
            orgName.trim().isEmpty() ||
            orgType.trim().isEmpty() ||
            orgEmail.trim().isEmpty() ||
            adminName.trim().isEmpty() ||
            adminEmail.trim().isEmpty() ||
            password.isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(
                "{\"success\":false,\"message\":\"Please fill all required fields.\"}"
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

        // Create Organization object
        Organization org = new Organization(
            null,
            orgName.trim(),
            orgType,
            orgEmail.trim(),
            phone
        );

        // Create Admin object
        // Password is hashed inside OrganizationServiceImpl
        User admin = new User(
            0,
            adminName.trim(),
            adminEmail.trim(),
            password,
            "ORG_ADMIN"
        );

        boolean result = service.registerOrganization(org, admin);

        if (result) {

            response.setStatus(HttpServletResponse.SC_OK);

            response.getWriter().print(
                "{\"success\":true," +
                "\"message\":\"Organization registered successfully!\"," +
                "\"organizationCode\":\"" + org.getOrganizationCode() + "\"}"
            );

        } else {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().print(
                "{\"success\":false," +
                "\"message\":\"Registration failed. Please check your details and try again.\"}"
            );
        }
    }//closing method
    
    
 // Allow frontend requests from VS Code Live Server
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
    
    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response)
                             throws ServletException, IOException {

        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
