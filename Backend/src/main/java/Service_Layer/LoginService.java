
package Service_Layer;

import Model_Layer.User;

public interface LoginService {

    User login(String organizationCode, String email, String password);

}
