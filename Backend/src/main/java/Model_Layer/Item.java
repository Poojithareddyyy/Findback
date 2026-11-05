package Model_Layer;

public class Item {

    private int itemId;
    private int organizationId;
    private int reportedBy;

    private String itemName;
    private String category;
    private String description;
    private String location;
    private String itemDate;
    private String itemType;
    private String status;
    private int matchScore;

    public Item(int organizationId, int reportedBy,
                String itemName, String category,
                String description, String location,
                String itemDate, String itemType,
                String status) {

        this.organizationId = organizationId;
        this.reportedBy = reportedBy;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.location = location;
        this.itemDate = itemDate;
        this.itemType = itemType;
        this.status = status;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public int getReportedBy() {
        return reportedBy;
    }

    public String getItemName() {
        return itemName;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getItemDate() {
        return itemDate;
    }

    public String getItemType() {
        return itemType;
    }

    public String getStatus() {
        return status;
    }
    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }
}