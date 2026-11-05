package Controller;

import java.io.IOException;
import java.security.MessageDigest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Model_Layer.User;
import Model_Layer.Claim;
import Data_Layer.SessionDAO;
import Service_Layer.ClaimService;
import Service_Implementation.ClaimServiceImpl;

public class ClaimServlet extends HttpServlet {

    private ClaimService service = new ClaimServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request,
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

            if (user == null || !"MEMBER".equals(user.getRole())) {
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Unauthorized access.\"}"
                );
                return;
            }

            int itemId = Integer.parseInt(request.getParameter("itemId"));
            String claimDetails = request.getParameter("claimDetails");

            if (claimDetails == null || claimDetails.trim().isEmpty()) {
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Please provide claim details.\"}"
                );
                return;
            }

            Claim claim = new Claim(
                user.getOrganizationId(),
                itemId,
                user.getUserId(),
                claimDetails.trim(),
                "PENDING"
            );

            boolean success = service.submitClaim(claim);

            if (success) {
                response.getWriter().write(
                    "{\"success\":true,\"message\":\"Claim submitted successfully! Waiting for admin verification.\"}"
                );
            } else {
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Item is unavailable or claim submission failed.\"}"
                );
            }

        } catch (NumberFormatException e) {
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid item ID.\"}"
            );

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Something went wrong.\"}"
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
}