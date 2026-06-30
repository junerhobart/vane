package org.oddlama.vane.permissions;

import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class LuckPermsGroups {

    private final Permissions module;
    private LuckPerms luck_perms = null;

    public LuckPermsGroups(final Permissions module) {
        this.module = module;
    }

    public boolean is_available() {
        if (luck_perms != null) {
            return true;
        }

        final var plugin = module.getServer().getPluginManager().getPlugin("LuckPerms");
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }

        try {
            luck_perms = LuckPermsProvider.get();
            return true;
        } catch (IllegalStateException e) {
            module.log.warning("LuckPerms is loaded, but its API is not available.");
            return false;
        }
    }

    public boolean add_parent_group(final OfflinePlayer player, final String group, final CommandSender actor) {
        if (!is_available()) {
            return false;
        }

        final var added = new AtomicBoolean(false);
        final var uuid = player.getUniqueId();
        final var node = InheritanceNode.builder(group).build();

        try {
            luck_perms.getUserManager().modifyUser(uuid, user -> added.set(user.data().add(node).wasSuccessful())).join();
        } catch (CompletionException e) {
            module.log.warning("Failed to assign LuckPerms group '" + group + "' to " + uuid + ": " + e.getMessage());
            return false;
        }

        if (added.get()) {
            module.log.info(
                "[audit] LuckPerms group " +
                group +
                " assigned to " +
                uuid +
                " (" +
                player.getName() +
                ") by " +
                actor.getName()
            );
        }

        return true;
    }
}
