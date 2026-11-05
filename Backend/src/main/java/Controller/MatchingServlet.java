
package Controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Model_Layer.User;
import Model_Layer.Item;

import Data_Layer.SessionDAO;

import Service_Layer.ItemService;
import Service_Implementation.ItemServiceImpl;

@WebServlet("/MatchingServlet")
public class MatchingServlet extends HttpServlet {

    private String hashToken(String token) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(token.getBytes("UTF-8"));

        StringBuilder hex = new StringBuilder();

        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            String header = request.getHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Please login.\"}"
                );
                return;
            }

            String token = header.substring(7);

            User user = new SessionDAO()
                    .getUserByTokenHash(hashToken(token));

            if (user == null || !"MEMBER".equals(user.getRole())) {
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Unauthorized access.\"}"
                );
                return;
            }

            String itemName = request.getParameter("itemName");
            String category = request.getParameter("category");
            String location = request.getParameter("location");
            String itemDate = request.getParameter("itemDate");

            if (itemName == null || itemName.trim().isEmpty()
                    || category == null || category.trim().isEmpty()
                    || location == null || location.trim().isEmpty()
                    || itemDate == null || itemDate.trim().isEmpty()) {

                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Please provide all item details.\"}"
                );
                return;
            }

            ItemService service = new ItemServiceImpl();

            List<Item> items = service.getMatchingItems(
                user.getOrganizationId(),
                itemName,
                category,
                location,
                itemDate
            );

            StringBuilder json = new StringBuilder();

            json.append("{\"success\":true,\"items\":[");

            for (int i = 0; i < items.size(); i++) {

                Item item = items.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"itemId\":").append(item.getItemId()).append(",")
                    .append("\"itemName\":\"").append(escape(item.getItemName())).append("\",")
                    .append("\"category\":\"").append(escape(item.getCategory())).append("\",")
                    .append("\"location\":\"").append(escape(item.getLocation())).append("\",")
                    .append("\"itemDate\":\"").append(escape(item.getItemDate())).append("\",")
                    .append("\"matchScore\":").append(item.getMatchScore())
                    .append("}");
            }

            json.append("]}");

            response.getWriter().write(json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to find matching items.\"}"
            );
        }
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }
}
