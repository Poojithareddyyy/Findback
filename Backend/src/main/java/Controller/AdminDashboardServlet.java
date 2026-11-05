
package Controller;

import java.io.IOException;
import java.security.MessageDigest;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Model_Layer.User;
import Data_Layer.SessionDAO;

import Service_Layer.ItemService;
import Service_Implementation.ItemServiceImpl;

import Service_Layer.ClaimService;
import Service_Implementation.ClaimServiceImpl;

@WebServlet("/AdminDashboardServlet")
public class AdminDashboardServlet extends HttpServlet {

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

            User admin = new SessionDAO()
                    .getUserByTokenHash(hashToken(token));

            if (admin == null || !"ORG_ADMIN".equals(admin.getRole())) {
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Unauthorized access.\"}"
                );
                return;
            }

            int orgId = admin.getOrganizationId();

            ItemService itemService = new ItemServiceImpl();
            ClaimService claimService = new ClaimServiceImpl();

            // Existing dashboard counts
            int lost = itemService.getItemCount(orgId, "LOST_OPEN");

            int foundReported = itemService.getItemCount(orgId, "FOUND_REPORTED");

            int available = itemService.getItemCount(orgId, "AVAILABLE");

            int claimPendingItems = itemService.getItemCount(orgId, "CLAIM_PENDING");

            int claimApprovedItems = itemService.getItemCount(orgId, "CLAIM_APPROVED");

            int returned = itemService.getItemCount(orgId, "RETURNED");

            int totalItems = lost + foundReported + available
                    + claimPendingItems + claimApprovedItems + returned;

            int pendingClaims = claimService.getClaimCount(orgId, "PENDING");

            int approvedClaims = claimService.getClaimCount(orgId, "APPROVED");

            int collectedClaims = claimService.getClaimCount(orgId, "COLLECTED");


            // Analytics counts

            int totalReports = totalItems;

            int foundItems = foundReported + available
                    + claimPendingItems + claimApprovedItems + returned;

            int totalClaims = claimService.getTotalClaims(orgId);

            int returnedItems = returned;


            // JSON response
            String json = "{"
                + "\"success\":true,"
                + "\"totalItems\":" + totalItems + ","
                + "\"lostItems\":" + lost + ","
                + "\"foundReported\":" + foundReported + ","
                + "\"availableItems\":" + available + ","
                + "\"pendingClaims\":" + pendingClaims + ","
                + "\"approvedClaims\":" + approvedClaims + ","
                + "\"collectedClaims\":" + collectedClaims + ","
                + "\"returnedItems\":" + returnedItems + ","
                + "\"totalReports\":" + totalReports + ","
                + "\"foundItems\":" + foundItems + ","
                + "\"totalClaims\":" + totalClaims
                + "}";

            response.getWriter().write(json);

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to load dashboard.\"}"
            );
        }
    }
}
