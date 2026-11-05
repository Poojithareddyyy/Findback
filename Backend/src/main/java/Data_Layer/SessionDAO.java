
package Data_Layer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Statement;

import JarJdbc.DBConn;
import Model_Layer.User;

public class SessionDAO {

    public boolean saveSession(int userId, int organizationId,
                               String tokenHash, Timestamp expiresAt) {

        Connection con = null;

        try {

            con = DBConn.MyConnection("findback_user_db");

            if (con == null) {
                throw new Exception("Database connection failed");
            }

            String query =
                "INSERT INTO sessions " +
                "(user_id, organization_id, token_hash, expires_at) " +
                "VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, userId);
            ps.setInt(2, organizationId);
            ps.setString(3, tokenHash);
            ps.setTimestamp(4, expiresAt);

            return ps.executeUpdate() > 0;

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
        
        
    }//savesession-close
    
    public User getUserByTokenHash(String tokenHash) {

        User user = null;

        String query = "SELECT u.user_id, u.organization_id, " +
                       "u.full_name, u.email, u.role " +
                       "FROM sessions s " +
                       "JOIN users u ON s.user_id = u.user_id " +
                       "JOIN organizations o ON s.organization_id = o.organization_id " +
                       "WHERE s.token_hash = ? " +
                       "AND s.expires_at > NOW() " +
                       "AND o.status = 'ACTIVE'";

        try (Connection con = DBConn.MyConnection("findback_user_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, tokenHash);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    user = new User(
                        rs.getInt("organization_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        null,
                        rs.getString("role")
                    );

                    user.setUserId(rs.getInt("user_id"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }//validatesession-closing
    
    
    public boolean deleteSession(String tokenHash) {

        String query = "DELETE FROM sessions WHERE token_hash = ?";

        try (Connection con = DBConn.MyConnection("findback_user_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, tokenHash);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }//deleteession-closing
    
}
