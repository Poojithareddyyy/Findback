
package Service_Implementation;

import org.mindrot.jbcrypt.BCrypt;

import Data_Layer.LoginDAO;
import Model_Layer.User;
import Service_Layer.LoginService;

public class LoginServiceImpl implements LoginService {

    LoginDAO dao = new LoginDAO();

    @Override
    public User login(String organizationCode, String email, String password) {

        // Get user details from database
        User user = dao.login(organizationCode, email);

        // Check whether user exists
        if (user == null) {
            return null;
        }

        // Verify entered password against stored hash
        boolean passwordMatches = BCrypt.checkpw(
            password,
            user.getPasswordHash()
        );

        if (passwordMatches) {
            return user;
        }

        return null;
    }
}
