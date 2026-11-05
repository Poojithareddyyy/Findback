package Controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Model_Layer.User;
import Model_Layer.Claim;
import Data_Layer.SessionDAO;
import Service_Layer.ClaimService;
import Service_Implementation.ClaimServiceImpl;

public class AdminClaimsServlet extends HttpServlet {

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
            User user = sessionDAO.getUserByTokenHash(tokenHash);

            if (user == null || !"ORG_ADMIN".equals(user.getRole())) {
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Unauthorized access.\"}"
                );
                return;
            }

            List<Claim> claims =
                service.getPendingClaims(user.getOrganizationId());

            StringBuilder json = new StringBuilder();

            json.append("{\"success\":true,\"claims\":[");

            for (int i = 0; i < claims.size(); i++) {

                Claim c = claims.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"claimId\":").append(c.getClaimId()).append(",")
                    .append("\"itemId\":").append(c.getItemId()).append(",")
                    .append("\"userId\":").append(c.getUserId()).append(",")
                    .append("\"claimDetails\":\"")
                    .append(escapeJSON(c.getClaimDetails())).append("\",")
                    .append("\"status\":\"")
                    .append(escapeJSON(c.getStatus())).append("\"")
                    .append("}");
            }

            json.append("]}");

            response.getWriter().write(json.toString());

        } catch (Exception e) {
            e.printStackTrace();

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to load claims.\"}"
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