package Controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Model_Layer.User;
import Data_Layer.SessionDAO;
import Service_Layer.ItemService;
import Service_Implementation.ItemServiceImpl;

public class ItemServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ItemService itemService = new ItemServiceImpl();

    private void setCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin",
                "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods",
                "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers",
                "Content-Type, Authorization");
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
            // 1. Get login token
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Please login first.\"}");
                return;
            }

            String token = authHeader.substring(7);
            String tokenHash = hashToken(token);

            // 2. Verify session
            SessionDAO sessionDAO = new SessionDAO();
            User user = sessionDAO.getUserByTokenHash(tokenHash);

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Invalid or expired session.\"}");
                return;
            }

            // 3. Only members can report items
            if (!"MEMBER".equals(user.getRole())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Only members can report items.\"}");
                return;
            }

            // 4. Get form details
            String itemName = request.getParameter("itemName");
            String category = request.getParameter("category");
            String description = request.getParameter("description");
            String location = request.getParameter("location");
            String itemDate = request.getParameter("itemDate");
            String itemType = request.getParameter("itemType");

            if (itemName == null || itemName.trim().isEmpty()
                    || category == null || category.trim().isEmpty()
                    || location == null || location.trim().isEmpty()
                    || itemDate == null || itemDate.trim().isEmpty()
                    || itemType == null
                    || (!itemType.equalsIgnoreCase("LOST")
                        && !itemType.equalsIgnoreCase("FOUND"))) {

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Please provide all required details.\"}");
                return;
            }

            // 5. Save item using logged-in user's details
            boolean saved = itemService.reportItem(
                    user.getOrganizationId(),
                    user.getUserId(),
                    itemName.trim(),
                    category,
                    description,
                    location.trim(),
                    itemDate,
                    itemType
            );

            if (saved) {
                response.getWriter().write(
                        "{\"success\":true,\"message\":\"Item reported successfully.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Unable to save item.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Server error while reporting item.\"}");
        }
    }
}