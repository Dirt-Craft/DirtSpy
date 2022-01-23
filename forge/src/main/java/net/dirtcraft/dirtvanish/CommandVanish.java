package net.dirtcraft.dirtvanish;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.dirtcommons.core.api.ForgePlayer;
import net.dirtcraft.dirtcommons.text.Colors;
import net.dirtcraft.dirtcommons.text.Styles;
import net.dirtcraft.dirtcommons.user.CommonsPlayer;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.*;
import net.minecraft.util.text.StringTextComponent;

public class CommandVanish implements Command<CommandSource> {
    public static String[] COMMAND_ALIASES = {"v", "vanish"};

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (!(context.getSource().getEntity() instanceof ForgePlayer)) {
            context.getSource()
                    .sendFailure(new StringTextComponent("Only players can use this command!")
                            .withStyle(Styles.as(Colors.RED)));
            return -1;
        }
        ForgePlayer holder = (ForgePlayer) context.getSource().getEntity();
        setVanishLevel((short) (holder.getVanishLevel() == 0? 1 : 0), context, holder);
        return 0;
    }

    public int setVanishLevel(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        if (!(ctx.getSource().getEntity() instanceof ForgePlayer)) {
            ctx.getSource()
                    .sendFailure(new StringTextComponent("Only players can use this command!")
                            .withStyle(Styles.as(Colors.RED)));
            return -1;
        }
        short value = ctx.getArgument("value", Integer.class).shortValue();
        ForgePlayer holder = (ForgePlayer) ctx.getSource().getEntity();
        setVanishLevel(value, ctx, holder);
        return 0;
    }

    public int setViewLevel(CommandContext<CommandSource> context){
        short level = context.getArgument("value", Integer.class).shortValue();
        ForgePlayer holder = (ForgePlayer) context.getSource().getEntity();
        ServerPlayerEntity sep = (ServerPlayerEntity) context.getSource().getEntity();
        if (sep == null) return -1;
        else if (level > 0) {
            level = holder.getMetaOrDefault(Permissions.META_VIEW_MAX, Short::valueOf, (short) 0);
        }
        holder.setVanishViewLevel(level);
        return Command.SINGLE_SUCCESS;
    }

    private void setVanishLevel(short level, CommandContext<CommandSource> context, CommonsPlayer agent) {
        ServerPlayerEntity sep = (ServerPlayerEntity) context.getSource().getEntity();
        ForgePlayer holder = (ForgePlayer) context.getSource().getEntity();
        if (sep == null) return;
        else if (level > 1) {
            level = holder.getMetaOrDefault(Permissions.META_LEVEL_MAX, Short::valueOf, (short) 1);
        }
        String message = level == 0? "You are no longer vanished!": "You are now vanished!";
        agent.setVanishLevel(level);

        sep.level.players().forEach(
                player -> {
                    if (player == agent) return;
                    ServerPlayNetHandler connection = ((ServerPlayerEntity) player).connection;
                    if ((canSeePlayer((CommonsPlayer) player, agent))) {
                        connection.send(new SPlayerListItemPacket(SPlayerListItemPacket.Action.ADD_PLAYER, sep));
                        connection.send(new SSpawnPlayerPacket(sep));
                    } else {
                        connection.send(new SPlayerListItemPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER, sep));
                        connection.send(new SDestroyEntitiesPacket(sep.getId()));
                    }
                }
        );

        context.getSource().sendSuccess(new StringTextComponent(message).withStyle(Styles.as(Colors.GREY)), false);
    }

    private boolean canSeePlayer(CommonsPlayer viewer, CommonsPlayer target) {
        return viewer.getVanishViewLevel() >= target.getVanishLevel();
    }


}
