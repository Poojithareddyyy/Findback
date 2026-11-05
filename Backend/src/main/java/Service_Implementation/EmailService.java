package Service_Implementation;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

import Model_Layer.User;

public class EmailService {

    public static void sendLoginAcknowledgment(User user) {

        try {
            Properties config = new Properties();

            InputStream input = EmailService.class
                    .getClassLoader()
                    .getResourceAsStream("Email.properties");

            if (input == null) {
                System.out.println("Email.properties not found!");
                return;
            }

            config.load(input);
            input.close();

            String username = config.getProperty("mail.username");
            String password = config.getProperty("mail.password");
            System.out.println("Email username loaded: " + username);
            System.out.println("App password length: " + password.length());

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(user.getEmail())
            );

            message.setSubject("Successful Login - FindBack");

            String body =
                "Hello " + user.getFullName() + ",\n\n" +
                "You have successfully logged in to FindBack.\n\n" +
                "Account Type: " + user.getRole() + "\n" +
                "Login Time: " + new Date() + "\n\n" +
                "If this login wasn't made by you, please contact your organization administrator.\n\n" +
                "Regards,\nFindBack Team";

            message.setText(body);

            Transport.send(message);

            System.out.println("Login acknowledgment email sent!");

        } catch (Exception e) {
            System.out.println("Email could not be sent: " + e.getMessage());
        }
    }
}