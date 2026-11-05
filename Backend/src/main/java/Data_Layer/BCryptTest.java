package Data_Layer;
import org.mindrot.jbcrypt.BCrypt;

public class BCryptTest {

    public static void main(String[] args) {

        String password = "poojitha123";

        // Encrypt password
        String hashedPassword = BCrypt.hashpw(
            password,
            BCrypt.gensalt()
        );

        System.out.println("Hashed Password: " + hashedPassword);

        // Verify password
        boolean result = BCrypt.checkpw(
            password,
            hashedPassword
        );

        System.out.println("Password Matched: " + result);
    }
}