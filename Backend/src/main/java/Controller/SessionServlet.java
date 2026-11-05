package Controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Data_Layer.SessionDAO;
import Model_Layer.User;

public class SessionServlet extends HttpServlet {

	private void setCorsHeaders(HttpServletResponse response) {

	    response.setHeader("Access-Control-Allow-Origin",
	                       "http://127.0.0.1:5500");

	    response.setHeader("Access-Control-Allow-Methods",
	                       "GET, OPTIONS");

	    response.setHeader("Access-Control-Allow-Headers",
	                       "Content-Type, Authorization");
	}

    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response)
                             throws ServletException, IOException {

        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Login required\"}"
            );
            return;
        }

        try {
            String token = authHeader.substring(7);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(token.getBytes("UTF-8"));

            StringBuilder tokenHashBuilder = new StringBuilder();

            for (byte b : hashBytes) {
                tokenHashBuilder.append(String.format("%02x", b));
            }

            String tokenHash = tokenHashBuilder.toString();

            SessionDAO dao = new SessionDAO();
            User user = dao.getUserByTokenHash(tokenHash);

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Session expired or invalid\"}"
                );
                return;
            }

            response.getWriter().write(
            	    "{\"success\":true," +
            	    "\"message\":\"Session valid\"," +
            	    "\"role\":\"" + user.getRole() + "\"," +
            	    "\"fullName\":\"" + user.getFullName().replace("\\", "\\\\").replace("\"", "\\\"") + "\"}"
            	);

        } catch (Exception e) {
            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Server error\"}"
            );
        }
    }
}