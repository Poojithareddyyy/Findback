
package Service_Implementation;

import org.mindrot.jbcrypt.BCrypt;

import Data_Layer.UserDAO;
import Model_Layer.User;
import Service_Layer.UserService;

public class UserServiceImpl implements UserService {

    UserDAO dao = new UserDAO();

    @Override
    public boolean registerMember(String organizationCode, User user) {

        // Hash the member password
        String hashedPassword = BCrypt.hashpw(
            user.getPasswordHash(),
            BCrypt.gensalt()
        );

        user.setPasswordHash(hashedPassword);

        // Send details to DAO
        return dao.registerMember(organizationCode, user);
    }
}
