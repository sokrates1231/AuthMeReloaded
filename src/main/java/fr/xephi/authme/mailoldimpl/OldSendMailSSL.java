package fr.xephi.authme.mailoldimpl;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.data.auth.PlayerAuth;
import fr.xephi.authme.settings.Settings;
import fr.xephi.authme.settings.properties.EmailSettings;
import org.bukkit.Bukkit;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author Xephi59
 */
public class OldSendMailSSL {

    private final AuthMe plugin;
    private final Settings settings;

    @Inject
    public OldSendMailSSL(AuthMe plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void main(final PlayerAuth auth, final String newPass) {
        String sendername;

        if (settings.getProperty(EmailSettings.MAIL_SENDER_NAME).isEmpty()) {
            sendername = settings.getProperty(EmailSettings.MAIL_ACCOUNT);
        } else {
            sendername = settings.getProperty(EmailSettings.MAIL_SENDER_NAME);
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", settings.getProperty(EmailSettings.SMTP_HOST));
        props.put("mail.smtp.socketFactory.port", String.valueOf(settings.getProperty(EmailSettings.SMTP_PORT)));
        props.put("mail.smtp.socketFactory.class",
            "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", String.valueOf(settings.getProperty(EmailSettings.SMTP_PORT)));

        Session session = Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        settings.getProperty(EmailSettings.MAIL_ACCOUNT),
                        settings.getProperty(EmailSettings.MAIL_PASSWORD));
                }
            });

        try {

            final Message message = new MimeMessage(session);
            try {
                message.setFrom(new InternetAddress(settings.getProperty(EmailSettings.MAIL_ACCOUNT), sendername));
            } catch (UnsupportedEncodingException uee) {
                message.setFrom(new InternetAddress(settings.getProperty(EmailSettings.MAIL_ACCOUNT)));
            }
            message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(auth.getEmail()));
            message.setSubject(settings.getProperty(EmailSettings.RECOVERY_MAIL_SUBJECT));
            message.setSentDate(new Date());
            String text = settings.getPasswordEmailMessage();
            text = text.replace("<playername>", auth.getNickname());
            text = text.replace("<servername>", plugin.getServer().getServerName());
            text = text.replace("<generatedpass>", newPass);
            message.setContent(text, "text/html");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(message);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });

            ConsoleLogger.info("Email sent to : " + auth.getNickname());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
