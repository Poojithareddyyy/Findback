
package Data_Layer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import JarJdbc.DBConn;
import Model_Layer.Organization;
import Model_Layer.User;

public class OrganizationDAO {

    public boolean registerOrganization(Organization org, User admin) {

        Connection con = null;

        try {
            con = DBConn.MyConnection("findback_user_db");

            if (con == null) {
                throw new Exception("Database connection failed");
            }

            
            con.setAutoCommit(false);

            // Step 1: Insert organization details
            String orgQuery =
                "INSERT INTO organizations " +
                "(organization_code, organization_name, organization_type, email, phone, status) " +
                "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";

            PreparedStatement ps1 =
                con.prepareStatement(orgQuery, Statement.RETURN_GENERATED_KEYS);

            ps1.setString(1, org.getOrganizationCode());
            ps1.setString(2, org.getOrganizationName());
            ps1.setString(3, org.getOrganizationType());
            ps1.setString(4, org.getEmail());
            ps1.setString(5, org.getPhone());

            ps1.executeUpdate();

            // Step 2: Get generated organization ID
            ResultSet rs = ps1.getGeneratedKeys();

            if (!rs.next()) {
                throw new Exception("Organization ID not generated");
            }

            int orgId = rs.getInt(1);

            // Step 3: Insert admin
            String userQuery =
                "INSERT INTO users " +
                "(organization_id, full_name, email, password_hash, role) " +
                "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement ps2 = con.prepareStatement(userQuery);

            ps2.setInt(1, orgId);
            ps2.setString(2, admin.getFullName());
            ps2.setString(3, admin.getEmail());
            ps2.setString(4, admin.getPasswordHash());
            ps2.setString(5, "ORG_ADMIN");

            ps2.executeUpdate();

            // Both inserts successful
            con.commit();

            return true;

        } catch (Exception e) {

            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            return false;

        } finally {

            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
