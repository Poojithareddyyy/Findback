package Model_Layer;

public class Claim {

    private int claimId;
    private int organizationId;
    private int itemId;
    private int userId;
    private String claimDetails;
    private String status;

    public Claim(int organizationId, int itemId, int userId,
                 String claimDetails, String status) {

        this.organizationId = organizationId;
        this.itemId = itemId;
        this.userId = userId;
        this.claimDetails = claimDetails;
        this.status = status;
    }

    public int getClaimId() {
        return claimId;
    }

    public void setClaimId(int claimId) {
        this.claimId = claimId;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getUserId() {
        return userId;
    }

    public String getClaimDetails() {
        return claimDetails;
    }

    public String getStatus() {
        return status;
    }
}