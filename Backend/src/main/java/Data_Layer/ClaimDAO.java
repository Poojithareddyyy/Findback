package Data_Layer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import JarJdbc.DBConn;
import Model_Layer.Claim;

import java.util.ArrayList;
import java.util.List;


public class ClaimDAO {

    public boolean submitClaim(Claim claim) {

        int claimId = 0;

        String checkQuery =
            "SELECT item_id FROM items " +
            "WHERE item_id=? AND organization_id=? " +
            "AND item_type='FOUND' AND status='AVAILABLE'";

        String insertClaim =
            "INSERT INTO claims " +
            "(organization_id,item_id,user_id,claim_details,status) " +
            "VALUES (?,?,?,?,?)";

        String updateItem =
            "UPDATE items SET status='CLAIM_PENDING' " +
            "WHERE item_id=? AND organization_id=? AND status='AVAILABLE'";

        String historyQuery =
            "INSERT INTO item_status_history " +
            "(item_id,old_status,new_status,updated_by) " +
            "VALUES (?,'AVAILABLE','CLAIM_PENDING',?)";

        try {
            // Step 1: Check item availability
            try (Connection itemCon =
                     DBConn.MyConnection("findback_item_db");
                 PreparedStatement check =
                     itemCon.prepareStatement(checkQuery)) {

                check.setInt(1, claim.getItemId());
                check.setInt(2, claim.getOrganizationId());

                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                }
            }

            // Step 2: Insert claim
            try (Connection claimCon =
                     DBConn.MyConnection("findback_claim_db");
                 PreparedStatement ps =
                     claimCon.prepareStatement(
                         insertClaim,
                         PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, claim.getOrganizationId());
                ps.setInt(2, claim.getItemId());
                ps.setInt(3, claim.getUserId());
                ps.setString(4, claim.getClaimDetails());
                ps.setString(5, "PENDING");

                if (ps.executeUpdate() == 0) {
                    return false;
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        claimId = keys.getInt(1);
                    }
                }
            }

            // Step 3: Change item status and record history
            try (Connection itemCon =
                     DBConn.MyConnection("findback_item_db")) {

                itemCon.setAutoCommit(false);

                try {
                    int updatedRows;

                    try (PreparedStatement update =
                             itemCon.prepareStatement(updateItem)) {

                        update.setInt(1, claim.getItemId());
                        update.setInt(2, claim.getOrganizationId());

                        updatedRows = update.executeUpdate();
                    }

                    // Another member may have claimed it already.
                    if (updatedRows == 0) {
                        itemCon.rollback();
                        deleteClaim(claimId);
                        return false;
                    }

                    try (PreparedStatement history =
                             itemCon.prepareStatement(historyQuery)) {

                        history.setInt(1, claim.getItemId());
                        history.setInt(2, claim.getUserId());
                        history.executeUpdate();
                    }

                    itemCon.commit();
                    return true;

                } catch (Exception e) {
                    itemCon.rollback();
                    deleteClaim(claimId);
                    throw e;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void deleteClaim(int claimId) {

        if (claimId == 0) return;

        String query = "DELETE FROM claims WHERE claim_id=?";

        try (Connection con =
                 DBConn.MyConnection("findback_claim_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, claimId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//closing methoid
    
    
    public List<Claim> getPendingClaims(int organizationId) {

        List<Claim> claims = new ArrayList<>();

        String query =
            "SELECT * FROM claims " +
            "WHERE organization_id = ? AND status IN ('PENDING', 'APPROVED') " +
            "ORDER BY claim_id DESC";

        try (Connection con =
                 DBConn.MyConnection("findback_claim_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, organizationId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Claim claim = new Claim(
                        rs.getInt("organization_id"),
                        rs.getInt("item_id"),
                        rs.getInt("user_id"),
                        rs.getString("claim_details"),
                        rs.getString("status")
                    );

                    claim.setClaimId(rs.getInt("claim_id"));

                    claims.add(claim);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return claims;
    }//closing method
    
    public boolean updateClaimStatus(int claimId, int organizationId,
            int adminId, String newStatus) {

    		if (!"APPROVED".equals(newStatus) &&
    				!"REJECTED".equals(newStatus)) {
    			return false;
    		}

    		String itemStatus = newStatus.equals("APPROVED")
    						? "CLAIM_APPROVED" : "AVAILABLE";

    		String updateClaim =
    				"UPDATE claims SET status=?, reviewed_by=? " +
    						"WHERE claim_id=? AND organization_id=? AND status='PENDING'";

    		String getItem =
    				"SELECT item_id FROM claims " +
    						"WHERE claim_id=? AND organization_id=? AND status='PENDING'";

    		String updateItem =
    				"UPDATE items SET status=? " +
    						"WHERE item_id=? AND organization_id=? AND status='CLAIM_PENDING'";

    		String history =
    				"INSERT INTO item_status_history " +
    						"(item_id, old_status, new_status, updated_by) " +
    						"VALUES (?, 'CLAIM_PENDING', ?, ?)";

    		int itemId;

    		try (Connection claimCon =
    				DBConn.MyConnection("findback_claim_db")) {

    			// Get the item connected to this claim.
    			try (PreparedStatement ps = claimCon.prepareStatement(getItem)) {
    				ps.setInt(1, claimId);
    				ps.setInt(2, organizationId);

    				try (ResultSet rs = ps.executeQuery()) {
    					if (!rs.next()) return false;
    					itemId = rs.getInt("item_id");
    				}
    			}	

    			claimCon.setAutoCommit(false);

    			try {
    				try (PreparedStatement ps = claimCon.prepareStatement(updateClaim)) {
    					ps.setString(1, newStatus);
    					ps.setInt(2, adminId);
    					ps.setInt(3, claimId);
    					ps.setInt(4, organizationId);

    					if (ps.executeUpdate() == 0) {
    						claimCon.rollback();
    						return false;
    					}
    				}

	    				try (Connection itemCon =
	    			DBConn.MyConnection("findback_item_db")) {

	    					itemCon.setAutoCommit(false);

	    					try {
	    						try (PreparedStatement ps =
	    								itemCon.prepareStatement(updateItem)) {

	    							ps.setString(1, itemStatus);
	    							ps.setInt(2, itemId);
	    							ps.setInt(3, organizationId);
	    							
	    							if (ps.executeUpdate() == 0) {
	    								itemCon.rollback();
	    								claimCon.rollback();
	    								return false;
	    							}
	    						}

	    						try (PreparedStatement ps =
	    								itemCon.prepareStatement(history)) {

	    							ps.setInt(1, itemId);
	    							ps.setString(2, itemStatus);
	    							ps.setInt(3, adminId);
	    							ps.executeUpdate();
	    						}

	    						itemCon.commit();
	    						claimCon.commit();
	    						return true;
	
	    					} catch (Exception e) {
	    						itemCon.rollback();
	    						claimCon.rollback();
	    						throw e;
	    					}
	    				}

    			} catch (Exception e) {
    				claimCon.rollback();
    				throw e;
    			}

    		} catch (Exception e) {
    			e.printStackTrace();
    		}

		    		return false;
    }//closing method
    
    public boolean confirmHandover(int claimId, int organizationId,
            int adminId) {

    	String getClaim =
    			"SELECT item_id, user_id FROM claims " +
    					"WHERE claim_id=? AND organization_id=? AND status='APPROVED'";

    	String updateClaim =
    			"UPDATE claims SET status='COLLECTED' " +
    					"WHERE claim_id=? AND organization_id=? AND status='APPROVED'";

    	String insertReturn =
    				"INSERT INTO returns " +
    						"(organization_id, claim_id, item_id, user_id, verified_by, return_date) " +
    						"VALUES (?, ?, ?, ?, ?, NOW())";
    	String updateItem =
    			"UPDATE items SET status='RETURNED' " +
    						"WHERE item_id=? AND organization_id=? AND status='CLAIM_APPROVED'";

    	String history =
    			"INSERT INTO item_status_history " +
    					"(item_id, old_status, new_status, updated_by) " +
    					"VALUES (?, 'CLAIM_APPROVED', 'RETURNED', ?)";

    	int itemId;
    	int userId;

    	try (Connection claimCon =
    			DBConn.MyConnection("findback_claim_db")) {

    		try (PreparedStatement ps = claimCon.prepareStatement(getClaim)) {
    			ps.setInt(1, claimId);
    			ps.setInt(2, organizationId);

    			try (ResultSet rs = ps.executeQuery()) {
    				if (!rs.next()) return false;

    				itemId = rs.getInt("item_id");
    				userId = rs.getInt("user_id");
    			}
    		}

    		claimCon.setAutoCommit(false);
		
    		try {
    			try (Connection itemCon =
    					DBConn.MyConnection("findback_item_db")) {

    				itemCon.setAutoCommit(false);

    				try {
    					try (PreparedStatement ps = itemCon.prepareStatement(updateItem)) {
    						ps.setInt(1, itemId);
    						ps.setInt(2, organizationId);

    						if (ps.executeUpdate() == 0) {
    							itemCon.rollback();
    							claimCon.rollback();
    							return false;
    						}
    					}

    					try (PreparedStatement ps = itemCon.prepareStatement(history)) {
    						ps.setInt(1, itemId);
    						ps.setInt(2, adminId);
    						ps.executeUpdate();
    					}

    					try (PreparedStatement ps = claimCon.prepareStatement(updateClaim)) {
    						ps.setInt(1, claimId);
    						ps.setInt(2, organizationId);

    						if (ps.executeUpdate() == 0) {
    							itemCon.rollback();
    							claimCon.rollback();
    							return false;
    						}
    					}

    					try (PreparedStatement ps = claimCon.prepareStatement(insertReturn)) {
    						ps.setInt(1, organizationId);
    						ps.setInt(2, claimId);
    						ps.setInt(3, itemId);
    						ps.setInt(4, userId);
    						ps.setInt(5, adminId);
    						ps.executeUpdate();
    					}

    					itemCon.commit();
    					claimCon.commit();

    					return true;

    				} catch (Exception e) {
    					itemCon.rollback();
    					claimCon.rollback();
    					throw e;
    				}
    			}

    		} catch (Exception e) {
    			claimCon.rollback();
    			throw e;
    		}

    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	return false;
    }//closing method
    
    public List<String[]> getCompletedReturns(int organizationId) {

        List<String[]> returns = new ArrayList<>();

        String query =
            "SELECT return_id, claim_id, item_id, user_id, verified_by, return_date " +
            "FROM returns WHERE organization_id=? ORDER BY return_id DESC";

        try (Connection con =
                 DBConn.MyConnection("findback_claim_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, organizationId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String[] row = {
                        String.valueOf(rs.getInt("return_id")),
                        String.valueOf(rs.getInt("claim_id")),
                        String.valueOf(rs.getInt("item_id")),
                        String.valueOf(rs.getInt("user_id")),
                        String.valueOf(rs.getInt("verified_by")),
                        String.valueOf(rs.getTimestamp("return_date"))
                    };

                    returns.add(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return returns;
    }//closing method
    
    public List<Claim> getMyClaims(int organizationId, int userId) {

        List<Claim> claims = new ArrayList<>();

        String query =
            "SELECT * FROM claims " +
            "WHERE organization_id = ? AND user_id = ? " +
            "ORDER BY claim_id DESC";

        try (Connection con =
                 DBConn.MyConnection("findback_claim_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, organizationId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Claim claim = new Claim(
                        rs.getInt("organization_id"),
                        rs.getInt("item_id"),
                        rs.getInt("user_id"),
                        rs.getString("claim_details"),
                        rs.getString("status")
                    );

                    claim.setClaimId(rs.getInt("claim_id"));
                    claims.add(claim);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return claims;
    }//closing meth
    
    public int getClaimCount(int organizationId, String status) {

        String query = "SELECT COUNT(*) FROM claims WHERE organization_id = ? AND status = ?";

        try (Connection con = DBConn.MyConnection("findback_claim_db");
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
    }//meth-close
    public int getTotalClaims(int organizationId) {

        String query = "SELECT COUNT(*) FROM claims WHERE organization_id = ?";

        try (Connection con = DBConn.MyConnection("findback_claim_db");
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, organizationId);

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