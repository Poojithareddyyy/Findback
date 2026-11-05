package Controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Data_Layer.SessionDAO;
import Model_Layer.Item;
import Model_Layer.User;
import Service_Layer.ItemService;
import Service_Implementation.ItemServiceImpl;

public class AdminItemsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ItemService itemService = new ItemServiceImpl();

    private void setCORS(HttpServletResponse response) {

        response.setHeader("Access-Control-Allow-Origin",
                "http://127.0.0.1:5500");

        response.setHeader("Access-Control-Allow-Methods",
                "GET, OPTIONS");

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

    private String escapeJSON(String value) {
        if (value == null) return "";

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        setCORS(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            // 1. Verify login token
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(401);
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
                response.setStatus(401);
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Session expired.\"}");
                return;
            }

            // 3. Only admin can access
            if (!"ORG_ADMIN".equals(user.getRole())) {
                response.setStatus(403);
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Admin access required.\"}");
                return;
            }

            // 4. Fetch organization's items
            List<Item> items =
                itemService.getItemsByOrganization(user.getOrganizationId());

            // 5. Convert records into JSON
            StringBuilder json = new StringBuilder();

            json.append("{\"success\":true,\"items\":[");

            for (int i = 0; i < items.size(); i++) {

                Item item = items.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"itemId\":").append(item.getItemId()).append(",")
                    .append("\"reportedBy\":").append(item.getReportedBy()).append(",")
                    .append("\"itemName\":\"").append(escapeJSON(item.getItemName())).append("\",")
                    .append("\"category\":\"").append(escapeJSON(item.getCategory())).append("\",")
                    .append("\"description\":\"").append(escapeJSON(item.getDescription())).append("\",")
                    .append("\"location\":\"").append(escapeJSON(item.getLocation())).append("\",")
                    .append("\"itemDate\":\"").append(escapeJSON(item.getItemDate())).append("\",")
                    .append("\"itemType\":\"").append(escapeJSON(item.getItemType())).append("\",")
                    .append("\"status\":\"").append(escapeJSON(item.getStatus())).append("\"")
                    .append("}");
            }

            json.append("]}");

            response.getWriter().write(json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to fetch item records.\"}");
        }
    }
}