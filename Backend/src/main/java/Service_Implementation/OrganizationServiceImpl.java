
package Service_Implementation;

import java.security.SecureRandom;

import org.mindrot.jbcrypt.BCrypt;

import Data_Layer.OrganizationDAO;
import Model_Layer.Organization;
import Model_Layer.User;
import Service_Layer.OrganizationService;

public class OrganizationServiceImpl implements OrganizationService {

    OrganizationDAO dao = new OrganizationDAO();

    
    public boolean registerOrganization(Organization org, User admin) {

        // Generate unique organization code
        String code = generateOrganizationCode();

        org.setOrganizationCode(code);

        // Hash admin password
        String hashedPassword =
            BCrypt.hashpw(admin.getPasswordHash(), BCrypt.gensalt());

        admin.setPasswordHash(hashedPassword);

        // Save organization and admin
        return dao.registerOrganization(org, admin);
    }

    private String generateOrganizationCode() {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();

        StringBuilder code = new StringBuilder("FB");

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }
}
