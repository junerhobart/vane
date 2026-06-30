package org.oddlama.vane.core.commands;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.oddlama.vane.annotation.command.Name;
import org.oddlama.vane.annotation.lang.LangMessage;
import org.oddlama.vane.core.Core;
import org.oddlama.vane.core.command.Command;
import org.oddlama.vane.core.command.argumentType.ModuleArgumentType;
import org.oddlama.vane.core.lang.TranslatedMessage;
import org.oddlama.vane.core.module.Context;
import org.oddlama.vane.core.module.Module;

@Name("vane")
public class Vane extends Command<Core> {

    @LangMessage
    private TranslatedMessage lang_reload_success;

    @LangMessage
    private TranslatedMessage lang_reload_fail;

    @LangMessage
    private TranslatedMessage lang_resource_pack_generate_success;

    @LangMessage
    private TranslatedMessage lang_resource_pack_generate_fail;

    public Vane(Context<Core> context) {
        super(context);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> get_command_base() {
        return super.get_command_base()
            .then(help())
            .then(
                literal("reload")
                    .executes(ctx -> {
                        reload_all(ctx.getSource().getSender());
                        return SINGLE_SUCCESS;
                    })
                    .then(
                        argument("module", ModuleArgumentType.module(get_module())).executes(ctx -> {
                            reload_module(ctx.getSource().getSender(), ctx.getArgument("module", Module.class));
                            return SINGLE_SUCCESS;
                        })
                    )
            )
            .then(
                literal("generate_resource_pack").executes(ctx -> {
                    generate_resource_pack(ctx.getSource().getSender());
                    return SINGLE_SUCCESS;
                })
            );
    }

    private void reload_module(CommandSender sender, Module<?> module) {
        if (module.reload_configuration()) {
            lang_reload_success.send(sender, "§bvane-" + module.get_name());
        } else {
            lang_reload_fail.send(sender, "§bvane-" + module.get_name());
        }
    }

    private void reload_all(CommandSender sender) {
        for (var m : get_module().core.get_modules()) {
            reload_module(sender, m);
        }
    }

    private void generate_resource_pack(CommandSender sender) {
        var file = get_module().generate_resource_pack();
        if (file != null) {
            lang_resource_pack_generate_success.send(sender, file.getAbsolutePath());
        } else {
            lang_resource_pack_generate_fail.send(sender);
        }
        if (sender instanceof Player) {
            var dist = get_module().resource_pack_distributor;
            dist.update_sha1(file);
            dist.send_resource_pack((Player) sender);
        }
    }
}
