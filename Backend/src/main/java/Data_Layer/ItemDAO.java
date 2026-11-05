package Data_Layer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;

import JarJdbc.DBConn;
import Model_Layer.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public boolean addItem(Item item) {

        String query = "INSERT INTO items " +
            "(organization_id, reported_by, item_name, category, " +
            "description, location, item_date, item_type, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con =
                 DBConn.MyConnection("findback_item_db")) {

            con.setAutoCommit(false);

            try (PreparedStatement ps =
                     con.prepareStatement(query,
                         PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, item.getOrganizationId());
                ps.setInt(2, item.getReportedBy());
                ps.setString(3, item.getItemName());
                ps.setString(4, item.getCategory());
                ps.setString(5, item.getDescription());
                ps.setString(6, item.getLocation());
                ps.setDate(7, Date.valueOf(item.getItemDate()));
                ps.setString(8, item.getItemType());
                ps.setString(9, item.getStatus());

                int rows = ps.executeUpdate();

                if (rows > 0) {

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (rs.next()) {

                            int itemId = rs.getInt(1);

                            String historyQuery =
                                "INSERT INTO item_status_history " +
                                "(item_id, old_status, new_status, updated_by) " +
                                "VALUES (?, ?, ?, ?)";

                            try (PreparedStatement historyPS =
                                     con.prepareStatement(historyQuery)) {

                                historyPS.setInt(1, itemId);
                                historyPS.setString(2, null);
                                historyPS.setString(3, item.getStatus());
                                historyPS.setInt(4, item.getReportedBy());

                                historyPS.executeUpdate();
                            }

                            con.commit();
                            return true;
                        }
                    }
                }

                con.rollback();

            } catch (Exception e) {
                con.rollback();
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }//closing addItem()
    
    
    public List<Item> getItemsByOrganization(int organizationId) {

        List<Item> items = new ArrayList<>();

        String query = "SELECT * FROM items WHERE organization_id = ? ORDER BY item_id DESC";

        try (Connection con = DBConn.MyConnection("findback_item_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, organizationId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Item item = new Item(
                        rs.getInt("organization_id"),
                        rs.getInt("reported_by"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getString("item_date"),
                        rs.getString("item_type"),
                        rs.getString("status")
                    );

                    item.setItemId(rs.getInt("item_id"));

                    items.add(item);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }//closemethod
    
    
    public boolean verifyFoundItem(int itemId, int organizationId,
            int adminId) {

    		String selectQuery =
    					"SELECT status FROM items WHERE item_id = ? " +
    								"AND organization_id = ? FOR UPDATE";

    		String updateQuery =
    				"UPDATE items SET status = 'AVAILABLE' " +
    						"WHERE item_id = ? AND organization_id = ?";

    		String historyQuery =
    				"INSERT INTO item_status_history " +
    						"(item_id, old_status, new_status, updated_by) " +
    						"VALUES (?, ?, 'AVAILABLE', ?)";

    		try (Connection con = DBConn.MyConnection("findback_item_db")) {

    			con.setAutoCommit(false);

    			try {
    				String oldStatus;

    				try (PreparedStatement ps = con.prepareStatement(selectQuery)) {
    					ps.setInt(1, itemId);
    					ps.setInt(2, organizationId);

    					try (ResultSet rs = ps.executeQuery()) {
    						if (!rs.next()) {
    							con.rollback();
    							return false;
    						}

    						oldStatus = rs.getString("status");
    					}
    				}

    				// Only found reports awaiting verification can be verified.
    				if (!"FOUND_REPORTED".equals(oldStatus)) {
    					con.rollback();
    					return false;
    				}

    				try (PreparedStatement ps = con.prepareStatement(updateQuery)) {
    					ps.setInt(1, itemId);
    					ps.setInt(2, organizationId);
    					ps.executeUpdate();
    				}

    				try (PreparedStatement ps = con.prepareStatement(historyQuery)) {
    					ps.setInt(1, itemId);
    					ps.setString(2, oldStatus);
    					ps.setInt(3, adminId);
    					ps.executeUpdate();
    				}

    				con.commit();
    				return true;
		
    			} catch (Exception e) {
    				con.rollback();
    				throw e;
    			}

    		} catch (Exception e) {
    			e.printStackTrace();
    		}

    		return false;
    }//closingmethod
    
    public List<Item> getAvailableItems(int organizationId) {

        List<Item> items = new ArrayList<>();

        String query = "SELECT * FROM items " +
                       "WHERE organization_id = ? " +
                       "AND item_type = 'FOUND' " +
                       "AND status = 'AVAILABLE' " +
                       "ORDER BY item_id DESC";

        try (Connection con = DBConn.MyConnection("findback_item_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, organizationId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Item item = new Item(
                        rs.getInt("organization_id"),
                        rs.getInt("reported_by"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getString("item_date"),
                        rs.getString("item_type"),
                        rs.getString("status")
                    );

                    item.setItemId(rs.getInt("item_id"));
                    items.add(item);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }//closing method
    
    public List<String[]> getMyItems(int organizationId, int userId) {

        List<String[]> items = new ArrayList<>();

        String query =
            "SELECT item_id, item_name, item_type, status, item_date " +
            "FROM items " +
            "WHERE organization_id = ? AND reported_by = ? " +
            "ORDER BY item_id DESC";

        try (Connection con =
                 DBConn.MyConnection("findback_item_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, organizationId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String[] row = {
                        String.valueOf(rs.getInt("item_id")),
                        rs.getString("item_name"),
                        rs.getString("item_type"),
                        rs.getString("status"),
                        String.valueOf(rs.getDate("item_date"))
                    };

                    items.add(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }//closing method
    
    public int getItemCount(int organizationId, String status) {

        String query =
            "SELECT COUNT(*) FROM items " +
            "WHERE organization_id = ? AND status = ?";

        try (Connection con =
                 DBConn.MyConnection("findback_item_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, organizationId);
            ps.setString(2, status);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
    
    
}