package fr.xephi.authme.service;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.xephi.authme.AuthMe;
import fr.xephi.authme.data.auth.PlayerCache;
import fr.xephi.authme.initialization.SettingsDependent;
import fr.xephi.authme.message.MessageKey;
import fr.xephi.authme.process.Management;
import fr.xephi.authme.settings.Settings;
import fr.xephi.authme.settings.properties.HooksSettings;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class to manage all BungeeCord related processes.
 */
public class BungeeService implements SettingsDependent, PluginMessageListener {

    private AuthMe plugin;
    private PlayerCache playerCache;
    private BukkitService bukkitService;
    private Management management;
    private CommonService service;

    private boolean isEnabled;
    private String bungeeServer;

    /*
     * Constructor.
     */
    @Inject
    BungeeService(AuthMe plugin, Settings settings, PlayerCache playerCache, BukkitService bukkitService,
                  Management management, CommonService service) {
        this.plugin = plugin;
        this.playerCache = playerCache;
        this.bukkitService = bukkitService;
        this.management = management;
        this.service = service;
        reload(settings);
    }

    /**
     * Send a player to a specified server. If no server is configured, this will
     * do nothing.
     *
     * @param player The player to send.
     */
    public void connectPlayer(Player player) {
        if (!isEnabled || bungeeServer.isEmpty()) {
            return;
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(bungeeServer);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    /**
     * Send the player's auth status to AuthMeBungee
     *
     * @param player The player
     */
    public void sendPlayerAuthStatus(Player player) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);

        try {
            out.writeUTF("AuthMeBungee");
            if(playerCache.isAuthenticated(player.getName())) {
                out.writeUTF("LOGIN:");
            } else {
                out.writeUTF("LOGOUT:");
            }
            out.writeUTF(player.getName());
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendPluginMessage(plugin, "BungeeCord", bout.toByteArray());
                }
            }.runTaskAsynchronously(plugin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload(Settings settings) {
        this.isEnabled = settings.getProperty(HooksSettings.BUNGEECORD);
        this.bungeeServer = settings.getProperty(HooksSettings.BUNGEECORD_SERVER);
        Messenger messenger = plugin.getServer().getMessenger();
        if (!this.isEnabled) {
            return;
        }
        if (!messenger.isOutgoingChannelRegistered(plugin, "BungeeCord")) {
            messenger.registerOutgoingPluginChannel(plugin, "BungeeCord");
        }
        if (!messenger.isIncomingChannelRegistered(plugin, "BungeeCord")) {
            messenger.registerIncomingPluginChannel(plugin, "BungeeCord", this);
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player ignored, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        String subchannel = in.readUTF();
        if (!subchannel.equals("AuthMeBungee")) {
            return;
        }

        String type = in.readUTF();
        if (!type.equals("AutoLogin")) {
            return;
        }

        String name = in.readUTF();
        Player player = bukkitService.getPlayerExact(name);
        if (player == null) {
            return;
        }

        if (playerCache.isAuthenticated(player.getName())) {
            return;
        }

        management.forceLogin(player);
        service.send(player, MessageKey.AUTHMEBUNGEE_AUTOLOGIN);
    }
}
