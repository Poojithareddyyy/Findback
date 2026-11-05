
package Service_Layer;

import Model_Layer.Organization;
import Model_Layer.User;

public interface OrganizationService {

    boolean registerOrganization(Organization org, User admin);

}
