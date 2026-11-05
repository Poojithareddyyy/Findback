
package Data_Layer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import JarJdbc.DBConn;
import Model_Layer.User;

public class UserDAO {

    public boolean registerMember(String organizationCode, User user) {

        Connection con = null;

        try {

            con = DBConn.MyConnection("findback_user_db");

            if (con == null) {
                throw new Exception("Database connection failed");
            }

            // Step 1: Find organization using organization code
            String orgQuery =
                "SELECT organization_id FROM organizations " +
                "WHERE organization_code = ? AND status = 'ACTIVE'";

            PreparedStatement ps1 = con.prepareStatement(orgQuery);

            ps1.setString(1, organizationCode);

            ResultSet rs = ps1.executeQuery();

            if (!rs.next()) {
                return false;
            }

            int orgId = rs.getInt("organization_id");

            // Step 2: Insert member into users table
            String userQuery =
                "INSERT INTO users " +
                "(organization_id, full_name, email, password_hash, role) " +
                "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement ps2 = con.prepareStatement(userQuery);

            ps2.setInt(1, orgId);
            ps2.setString(2, user.getFullName());
            ps2.setString(3, user.getEmail());
            ps2.setString(4, user.getPasswordHash());
            ps2.setString(5, "MEMBER");

            int rows = ps2.executeUpdate();

            return rows > 0;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        } finally {

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
