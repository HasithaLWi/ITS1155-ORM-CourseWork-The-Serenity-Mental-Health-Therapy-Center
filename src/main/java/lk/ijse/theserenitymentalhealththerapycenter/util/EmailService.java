package lk.ijse.theserenitymentalhealththerapycenter.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailService {
    private static final Properties properties = new Properties();
    private static final ExecutorService executor = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    public static void shutdown() {
        executor.shutdown();
    }

    static {
        try (InputStream input = EmailService.class.getClassLoader().getResourceAsStream("email.properties")) {
            if (input == null) {
                System.err.println("Unable to find email.properties. Falling back to default placeholders.");
                properties.put("email.smtp.host", "smtp.gmail.com");
                properties.put("email.smtp.port", "587");
                properties.put("email.smtp.auth", "true");
                properties.put("email.smtp.starttls.enable", "true");
                properties.put("email.username", "your_email@gmail.com");
                properties.put("email.password", "your_app_password");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void sendEmailAsync(String to, String subject, String body) {
        executor.submit(() -> {
            try {
                sendEmail(to, subject, body);
            } catch (Exception e) {
                System.err.println("Failed to send email to " + to + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public static void sendEmail(String to, String subject, String body) throws MessagingException {
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", properties.getProperty("email.smtp.host", "smtp.gmail.com"));
        mailProps.put("mail.smtp.port", properties.getProperty("email.smtp.port", "587"));
        mailProps.put("mail.smtp.auth", properties.getProperty("email.smtp.auth", "true"));
        mailProps.put("mail.smtp.starttls.enable", properties.getProperty("email.smtp.starttls.enable", "true"));
        mailProps.put("mail.smtp.ssl.protocols", "TLSv1.2");

        final String username = properties.getProperty("email.username");
        final String password = properties.getProperty("email.password");

        if (username == null || username.isEmpty() || username.contains("your_email")) {
            System.out.println("====== EMAIL SIMULATOR (No real credentials configured) ======");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Body:\n" + body);
            System.out.println("==============================================================");
            return;
        }

        Session session = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
        System.out.println("Email successfully sent to " + to);
    }
}
