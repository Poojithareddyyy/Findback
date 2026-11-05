package Service_Layer;

import java.util.List;
import Model_Layer.Claim;

public interface ClaimService {

    boolean submitClaim(Claim claim);

    List<Claim> getPendingClaims(int organizationId);

    boolean updateClaimStatus(int claimId, int organizationId,
                              int adminId, String newStatus);
    
    boolean confirmHandover(int claimId, int organizationId, int adminId);
    List<String[]> getCompletedReturns(int organizationId);
    List<Claim> getMyClaims(int organizationId, int userId);
    int getClaimCount(int organizationId, String status);
    int getTotalClaims(int organizationId);
}