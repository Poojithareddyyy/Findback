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
import Model_Layer.User;
import Model_Layer.Item;
import Service_Layer.ItemService;
import Service_Implementation.ItemServiceImpl;

public class SearchItemsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ItemService itemService = new ItemServiceImpl();

    private String hashToken(String token) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));

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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

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

            if (!"MEMBER".equals(user.getRole())) {
                response.setStatus(403);
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Member access required.\"}");
                return;
            }

            List<Item> items =
                itemService.getAvailableItems(user.getOrganizationId());

            StringBuilder json = new StringBuilder();

            json.append("{\"success\":true,\"items\":[");

            for (int i = 0; i < items.size(); i++) {

                Item item = items.get(i);

                if (i > 0) json.append(",");

                json.append("{")
                    .append("\"itemId\":").append(item.getItemId()).append(",")
                    .append("\"itemName\":\"").append(escapeJSON(item.getItemName())).append("\",")
                    .append("\"category\":\"").append(escapeJSON(item.getCategory())).append("\",")
                    .append("\"description\":\"").append(escapeJSON(item.getDescription())).append("\",")
                    .append("\"location\":\"").append(escapeJSON(item.getLocation())).append("\",")
                    .append("\"itemDate\":\"").append(escapeJSON(item.getItemDate())).append("\"")
                    .append("}");
            }

            json.append("]}");

            response.getWriter().write(json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(500);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to fetch found items.\"}");
        }
    }
}