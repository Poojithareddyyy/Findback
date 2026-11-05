package Controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Data_Layer.SessionDAO;
import Model_Layer.User;
import Service_Layer.ItemService;
import Service_Implementation.ItemServiceImpl;

public class VerifyItemServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ItemService itemService = new ItemServiceImpl();

    private void setCORS(HttpServletResponse response) {

        response.setHeader("Access-Control-Allow-Origin",
                "http://127.0.0.1:5500");

        response.setHeader("Access-Control-Allow-Methods",
                "POST, OPTIONS");

        response.setHeader("Access-Control-Allow-Headers",
                "Authorization, Content-Type");

        response.setHeader("Access-Control-Max-Age", "3600");
    }

    @Override
    protected void doOptions(HttpServletRequest request,
                             HttpServletResponse response) {
        setCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String hashToken(String token) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] hash = md.digest(
                token.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();

        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        setCORS(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // 1. Verify admin session
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(401);
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Please login first.\"}");
                return;
            }

            String token = authHeader.substring(7);
            String tokenHash = hashToken(token);

            SessionDAO sessionDAO = new SessionDAO();
            User user = sessionDAO.getUserByTokenHash(tokenHash);

            if (user == null) {
                response.setStatus(401);
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Session expired.\"}");
                return;
            }

            // 2. Only organization admin can verify
            if (!"ORG_ADMIN".equals(user.getRole())) {
                response.setStatus(403);
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Admin access required.\"}");
                return;
            }

            // 3. Get item ID
            String id = request.getParameter("itemId");

            if (id == null || !id.matches("\\d+")) {
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Invalid item ID.\"}");
                return;
            }

            int itemId = Integer.parseInt(id);

            // 4. Verify item
            boolean verified = itemService.verifyFoundItem(
                    itemId,
                    user.getOrganizationId(),
                    user.getUserId()
            );

            if (verified) {
                response.getWriter().write(
                    "{\"success\":true,\"message\":\"Found item verified successfully.\"}");
            } else {
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Item could not be verified. Check its current status.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();

            response.setStatus(500);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Server error while verifying item.\"}");
        }
    }
}