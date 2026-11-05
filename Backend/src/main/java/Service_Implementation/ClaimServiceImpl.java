package Service_Implementation;

import java.util.List;

import Data_Layer.ClaimDAO;
import Model_Layer.Claim;
import Service_Layer.ClaimService;

public class ClaimServiceImpl implements ClaimService {

    ClaimDAO dao = new ClaimDAO();

    @Override
    public boolean submitClaim(Claim claim) {
        return dao.submitClaim(claim);
    }

    @Override
    public List<Claim> getPendingClaims(int organizationId) {
        return dao.getPendingClaims(organizationId);
    }

    @Override
    public boolean updateClaimStatus(int claimId, int organizationId,
                                     int adminId, String newStatus) {
        return dao.updateClaimStatus(
            claimId, organizationId, adminId, newStatus
        );
    }
    @Override
    public boolean confirmHandover(int claimId, int organizationId, int adminId) {
        return dao.confirmHandover(claimId, organizationId, adminId);
    }
    
    @Override
    public List<String[]> getCompletedReturns(int organizationId) {
        return dao.getCompletedReturns(organizationId);
    }
    
    @Override
    public List<Claim> getMyClaims(int organizationId, int userId) {

        ClaimDAO dao = new ClaimDAO();

        return dao.getMyClaims(organizationId, userId);
    }
    
    @Override
    public int getClaimCount(int organizationId, String status) {
        ClaimDAO dao = new ClaimDAO();
        return dao.getClaimCount(organizationId, status);
    }
    
    @Override
    public int getTotalClaims(int organizationId) {
        return dao.getTotalClaims(organizationId);
    }
    
    
}