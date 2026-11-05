package Controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Model_Layer.User;
import Data_Layer.SessionDAO;
import Service_Layer.ClaimService;
import Service_Implementation.ClaimServiceImpl;

public class AdminReturnsServlet extends HttpServlet {

    private ClaimService service = new ClaimServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Please login first.\"}"
                );
                return;
            }

            String token = authHeader.substring(7);
            String tokenHash = hashToken(token);

            SessionDAO sessionDAO = new SessionDAO();
            User admin = sessionDAO.getUserByTokenHash(tokenHash);

            if (admin == null || !"ORG_ADMIN".equals(admin.getRole())) {
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Unauthorized access.\"}"
                );
                return;
            }

            List<String[]> returns =
                service.getCompletedReturns(admin.getOrganizationId());

            StringBuilder json = new StringBuilder();
            json.append("{\"success\":true,\"returns\":[");

            for (int i = 0; i < returns.size(); i++) {

                String[] r = returns.get(i);

                if (i > 0) json.append(",");

                json.append("{")
                    .append("\"returnId\":").append(r[0]).append(",")
                    .append("\"claimId\":").append(r[1]).append(",")
                    .append("\"itemId\":").append(r[2]).append(",")
                    .append("\"userId\":").append(r[3]).append(",")
                    .append("\"verifiedBy\":").append(r[4]).append(",")
                    .append("\"returnDate\":\"")
                    .append(escapeJSON(r[5])).append("\"")
                    .append("}");
            }

            json.append("]}");

            response.getWriter().write(json.toString());

        } catch (Exception e) {
            e.printStackTrace();

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to load returns.\"}"
            );
        }
    }

    private String hashToken(String token) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(token.getBytes("UTF-8"));

        StringBuilder hex = new StringBuilder();

        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }

    private String escapeJSON(String value) {

        if (value == null) return "";

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}