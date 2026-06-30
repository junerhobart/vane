package org.oddlama.vane.permissions.commands;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.oddlama.vane.annotation.command.Name;
import org.oddlama.vane.annotation.config.ConfigString;
import org.oddlama.vane.annotation.lang.LangMessage;
import org.oddlama.vane.annotation.persistent.Persistent;
import org.oddlama.vane.core.command.Command;
import org.oddlama.vane.core.command.argumentType.OfflinePlayerArgumentType;
import org.oddlama.vane.core.lang.TranslatedMessage;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.permissions.Permissions;

@Name("vouch")
public class Vouch extends Command<Permissions> {

    @LangMessage
    private TranslatedMessage lang_vouched;

    @LangMessage
    private TranslatedMessage lang_already_vouched;

    @ConfigString(
        def = "user",
        desc = "The permission group to assign when a player is vouched for the first time.",
        metrics = true
    )
    private String config_vouch_group;

    // Persistent storage
    @Persistent
    public Map<UUID, Set<UUID>> storage_vouched_by = new HashMap<>();

    public Vouch(Context<Permissions> context) {
        super(context);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> get_command_base() {
        return super.get_command_base()
            .then(help())
            .then(
                argument("offline_player", OfflinePlayerArgumentType.offlinePlayer()).executes(ctx -> {
                    vouch_for_player(
                        ctx.getSource().getSender(),
                        ctx.getArgument("offline_player", OfflinePlayer.class)
                    );
                    return SINGLE_SUCCESS;
                })
            );
    }

    private String player_name(final OfflinePlayer player) {
        return "§b" + (player.getName() == null ? player.getUniqueId().toString() : player.getName());
    }

    private void vouch_for_player(final CommandSender sender, final OfflinePlayer vouched_player) {
        if (!(sender instanceof Player)) {
            get_module().promote_player_to_group(vouched_player, config_vouch_group, sender);
            lang_vouched.send(sender, player_name(vouched_player));
            return;
        }

        final var player = (Player) sender;
        var vouched_by_set = storage_vouched_by.computeIfAbsent(vouched_player.getUniqueId(), k -> new HashSet<>());

        if (!vouched_by_set.add(player.getUniqueId())) {
            lang_already_vouched.send(sender, player_name(vouched_player));
            return;
        }

        if (vouched_by_set.size() == 1) {
            get_module().promote_player_to_group(vouched_player, config_vouch_group, sender);
        }

        lang_vouched.send(sender, player_name(vouched_player));
        mark_persistent_storage_dirty();
    }
}
