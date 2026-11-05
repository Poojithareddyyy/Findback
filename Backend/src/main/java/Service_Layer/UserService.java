
package Service_Layer;

import Model_Layer.User;

public interface UserService {

    boolean registerMember(String organizationCode, User user);

}
