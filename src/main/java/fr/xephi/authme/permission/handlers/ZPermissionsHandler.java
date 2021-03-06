package fr.xephi.authme.permission.handlers;

import fr.xephi.authme.permission.PermissionNode;
import fr.xephi.authme.permission.PermissionsSystemType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService;

import java.util.Collection;
import java.util.Map;

/**
 * Handler for zPermissions.
 *
 * @see <a href="https://dev.bukkit.org/projects/zpermissions">zPermissions Bukkit page</a>
 * @see <a href="https://github.com/ZerothAngel/zPermissions">zPermissions on Github</a>
 */
public class ZPermissionsHandler implements PermissionHandler {

    private ZPermissionsService zPermissionsService;

    public ZPermissionsHandler() throws PermissionHandlerException {
        // Set the zPermissions service and make sure it's valid
        ZPermissionsService zPermissionsService = Bukkit.getServicesManager().load(ZPermissionsService.class);
        if (zPermissionsService == null) {
            throw new PermissionHandlerException("Failed to get the ZPermissions service!");
        }
        this.zPermissionsService = zPermissionsService;
    }

    @Override
    public boolean addToGroup(Player player, String group) {
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "permissions player " + player.getName() + " addgroup " + group);
    }

    @Override
    public boolean hasGroupSupport() {
        return true;
    }

    @Override
    public boolean hasPermissionOffline(String name, PermissionNode node) {
        Map<String, Boolean> perms = zPermissionsService.getPlayerPermissions(null, null, name);
        if (perms.containsKey(node.getNode()))
            return perms.get(node.getNode());
        else
            return false;
    }

    @Override
    public boolean removeFromGroup(Player player, String group) {
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "permissions player " + player.getName() + " removegroup " + group);
    }

    @Override
    public boolean setGroup(Player player, String group) {
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "permissions player " + player.getName() + " setgroup " + group);
    }

    @Override
    public Collection<String> getGroups(Player player) {
        // TODO Gnat008 20160631: Use UUID not name?
        return zPermissionsService.getPlayerGroups(player.getName());
    }

    @Override
    public String getPrimaryGroup(Player player) {
        // TODO Gnat008 20160631: Use UUID not name?
        return zPermissionsService.getPlayerPrimaryGroup(player.getName());
    }

    @Override
    public PermissionsSystemType getPermissionSystem() {
        return PermissionsSystemType.Z_PERMISSIONS;
    }
}
