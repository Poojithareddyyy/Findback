package Model_Layer;

public class Organization {
	private int organizationId;
    private String organizationCode;
    private String organizationName;
    private String organizationType;
    private String email;
    private String phone;
    private String status;

    public Organization() {
    }

    public Organization(String organizationCode,
            String organizationName,
            String organizationType,
            String email,
            String phone) {

    	this.organizationCode = organizationCode;
    	this.organizationName = organizationName;
    	this.organizationType = organizationType;
    	this.email = email;
    	this.phone = phone;
    }

    public int getOrganizationId() {
    	return organizationId;
    }

    public void setOrganizationId(int organizationId) {
    	this.organizationId = organizationId;
    }

    public String getOrganizationCode() {
    	return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
    	this.organizationCode = organizationCode;
    }

    public String getOrganizationName() {
    	return organizationName;
    }

    public void setOrganizationName(String organizationName) {
    	this.organizationName = organizationName;
    }

    public String getOrganizationType() {
    	return organizationType;
    }

    public void setOrganizationType(String organizationType) {
    	this.organizationType = organizationType;
    }

    public String getEmail() {
    	return email;
    }

    public void setEmail(String email) {
    	this.email = email;
    }

    public String getPhone() {
    	return phone;
    }

    public void setPhone(String phone) {
    	this.phone = phone;
    }

    public String getStatus() {
    	return status;
    }

    public void setStatus(String status) {
    	this.status = status;
    }
}
