package fr.xephi.authme.process;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.output.MessageKey;
import fr.xephi.authme.output.Messages;
import fr.xephi.authme.permission.PermissionNode;
import fr.xephi.authme.settings.NewSetting;
import fr.xephi.authme.settings.domain.Property;
import fr.xephi.authme.util.BukkitService;
import fr.xephi.authme.util.ValidationService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitTask;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Service for asynchronous and synchronous processes.
 */
public class ProcessService {

    @Inject
    private NewSetting settings;
    @Inject
    private Messages messages;
    @Inject
    private AuthMe plugin;
    @Inject
    private ValidationService validationService;
    @Inject
    private BukkitService bukkitService;

    /**
     * Retrieve a property's value.
     *
     * @param property the property to retrieve
     * @param <T> the property type
     * @return the property's value
     */
    public <T> T getProperty(Property<T> property) {
        return settings.getProperty(property);
    }

    /**
     * Return the settings manager.
     *
     * @return settings manager
     */
    public NewSetting getSettings() {
        return settings;
    }

    /**
     * Send a message to the command sender.
     *
     * @param sender the command sender
     * @param key the message key
     */
    public void send(CommandSender sender, MessageKey key) {
        messages.send(sender, key);
    }

    /**
     * Send a message to the command sender with the given replacements.
     *
     * @param sender the command sender
     * @param key the message key
     * @param replacements the replacements to apply to the message
     */
    public void send(CommandSender sender, MessageKey key, String... replacements) {
        messages.send(sender, key, replacements);
    }

    /**
     * Retrieve a message.
     *
     * @param key the key of the message
     * @return the message, split by line
     */
    public String[] retrieveMessage(MessageKey key) {
        return messages.retrieve(key);
    }

    /**
     * Retrieve a message as one piece.
     *
     * @param key the key of the message
     * @return the message
     */
    public String retrieveSingleMessage(MessageKey key) {
        return messages.retrieveSingle(key);
    }

    /**
     * Run a task.
     *
     * @param task the task to run
     * @return the assigned task id
     */
    public BukkitTask runTask(Runnable task) {
        return plugin.getServer().getScheduler().runTask(plugin, task);
    }

    /**
     * Run a task at a later time.
     *
     * @param task the task to run
     * @param delay the delay before running the task
     * @return the assigned task id
     */
    public BukkitTask runTaskLater(Runnable task, long delay) {
        return plugin.getServer().getScheduler().runTaskLater(plugin, task, delay);
    }

    /**
     * Schedule a synchronous delayed task.
     *
     * @param task the task to schedule
     * @return the task id
     */
    public int scheduleSyncDelayedTask(Runnable task) {
        return plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task);
    }

    /**
     * Emit an event.
     *
     * @param event the event to emit
     */
    public void callEvent(Event event) {
        plugin.getServer().getPluginManager().callEvent(event);
    }

    /**
     * Return the plugin instance.
     *
     * @return AuthMe instance
     */
    public AuthMe getAuthMe() {
        return plugin;
    }

    /**
     * Verifies whether a password is valid according to the plugin settings.
     *
     * @param password the password to verify
     * @param username the username the password is associated with
     * @return message key with the password error, or {@code null} if password is valid
     */
    public MessageKey validatePassword(String password, String username) {
        return validationService.validatePassword(password, username);
    }

    public boolean validateEmail(String email) {
        return validationService.validateEmail(email);
    }

    public boolean isEmailFreeForRegistration(String email, CommandSender sender) {
        return validationService.isEmailFreeForRegistration(email, sender);
    }

    public Collection<? extends Player> getOnlinePlayers() {
        return bukkitService.getOnlinePlayers();
    }

    public BukkitService getBukkitService() {
        return bukkitService;
    }

    public boolean hasPermission(Player player, PermissionNode node) {
        return plugin.getPermissionsManager().hasPermission(player, node);
    }

}