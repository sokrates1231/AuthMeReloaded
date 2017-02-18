package fr.xephi.authme.command.executable.authme.debug;

import fr.xephi.authme.data.auth.PlayerAuth;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.mail.SendMailSSL;
import fr.xephi.authme.mailoldimpl.OldSendMailSSL;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.List;

/**
 * Sends out a test email.
 */
class OldTestEmailSender implements DebugSection {

    @Inject
    private DataSource dataSource;

    @Inject
    private SendMailSSL newSendMailSSL;

    @Inject
    private OldSendMailSSL sendMailSSL;


    @Override
    public String getName() {
        return "oldmail";
    }

    @Override
    public String getDescription() {
        return "Sends out a test email the AuthMe 3.4 way";
    }

    @Override
    public void execute(CommandSender sender, List<String> arguments) {
        if (!newSendMailSSL.hasAllInformation()) {
            sender.sendMessage(ChatColor.RED + "You haven't set all required configurations in config.yml " +
                "for sending emails. Please check your config.yml");
            return;
        }

        PlayerAuth auth = getAuth(sender, arguments);

        // getAuth() takes care of informing the sender of the error if auth == null
        if (auth != null) {
            sendMailSSL.main(auth, "test-test-ignore-me");
            sender.sendMessage("Test email sent to " + auth.getRealName());
        }
    }

    private PlayerAuth getAuth(CommandSender sender, List<String> arguments) {
        String name = arguments.isEmpty() ? sender.getName() : arguments.get(0);
        PlayerAuth auth = dataSource.getAuth(name);
        if (auth == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + name + "' is not registered. "
                + "Use /authme debug oldmail <name> to specify a user to send to.");
            return null;
        }
        String email = auth.getEmail();
        if (email == null || "your@email.com".equals(email)) {
            sender.sendMessage(ChatColor.RED + "No email set to the account! Please use /email add");
            return null;
        }
        return auth;
    }
}
