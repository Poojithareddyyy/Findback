
package Data_Layer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import JarJdbc.DBConn;
import Model_Layer.User;

public class LoginDAO {

    public User login(String organizationCode, String email) {

        Connection con = null;
        User user = null;

        try {

            con = DBConn.MyConnection("findback_user_db");

            if (con == null) {
                throw new Exception("Database connection failed");
            }

            String query =
                "SELECT u.user_id, u.organization_id, u.full_name, " +
                "u.email, u.password_hash, u.role " +
                "FROM users u " +
                "JOIN organizations o " +
                "ON u.organization_id = o.organization_id " +
                "WHERE o.organization_code = ? " +
                "AND o.status = 'ACTIVE' " +
                "AND u.email = ?";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, organizationCode);
            ps.setString(2, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                user = new User();

                user.setUserId(rs.getInt("user_id"));
                user.setOrganizationId(rs.getInt("organization_id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(rs.getString("role"));
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return user;
    }//meth
    
    public boolean markFirstLogin(int userId) {

        String query = "UPDATE users SET last_login = NOW() " +
                       "WHERE user_id = ? AND last_login IS NULL";

        try (Connection con = DBConn.MyConnection("findback_user_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
