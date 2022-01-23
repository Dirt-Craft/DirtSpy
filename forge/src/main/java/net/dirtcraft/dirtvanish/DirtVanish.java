package net.dirtcraft.dirtvanish;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.dirtcraft.dirtcommons.core.api.ForgePlayer;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Collection;

import static net.dirtcraft.dirtvanish.DirtVanish.MOD_ID;
import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

@Mod(MOD_ID)
public class DirtVanish {
    public static final String MOD_ID = "dirtvanish";
    public static Path CONFIG_PATH = FMLPaths.GAMEDIR.get().resolve(FMLPaths.CONFIGDIR.get()).resolve(MOD_ID);
    public VanishConfig config = new VanishConfig(CONFIG_PATH.resolve("userdata"));
    CommandVanish vanish = new CommandVanish();

    public DirtVanish(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void register(RegisterCommandsEvent event) {
        for (String base : CommandVanish.COMMAND_ALIASES) {
            LiteralCommandNode<CommandSource> vanish = literal(base)
                    .requires(src -> src.getEntity() instanceof ForgePlayer &&
                            ((ForgePlayer)src.getEntity()).hasPermission(Permissions.VANISH))
                    .executes(this.vanish).build();

            LiteralCommandNode<CommandSource> add = literal("track")
                    .then(argument("entity", EntityArgument.entities())
                            .requires(src -> src.getEntity() instanceof ForgePlayer &&
                                    ((ForgePlayer)src.getEntity()).hasPermission(Permissions.TRACK))
                            .executes(ctx -> {
                                ForgePlayer agent = (ForgePlayer) ctx.getSource().getEntity();
                                Collection<? extends Entity> value = EntityArgument.getEntities(ctx, "entity");
                                agent.addTrackedEntities(value);
                                return 0;
                            })).build();
            LiteralCommandNode<CommandSource> remove = literal("untrack")
                    .requires(src -> src.getEntity() instanceof ForgePlayer)
                    .executes(ctx -> {
                        ForgePlayer agent = (ForgePlayer) ctx.getSource().getEntity();
                        agent.clearTrackedEntities();
                        return 0;
                    }).then(argument("entity", EntityArgument.entities())
                            .requires(src -> src.getEntity() instanceof ForgePlayer &&
                                    ((ForgePlayer)src.getEntity()).hasPermission(Permissions.TRACK))
                            .executes(ctx -> {
                                ForgePlayer agent = (ForgePlayer) ctx.getSource().getEntity();
                                Collection<? extends Entity> value = EntityArgument.getEntities(ctx, "entity");
                                agent.removeTrackedEntities(value);
                                return 0;
                            })).build();
            LiteralCommandNode<CommandSource> chams = literal("showplayers")
                    .requires(src -> src.getEntity() instanceof ForgePlayer &&
                            ((ForgePlayer)src.getEntity()).hasPermission(Permissions.THERMALS))
                    .executes(ctx -> {
                        ForgePlayer agent = (ForgePlayer) ctx.getSource().getEntity();
                        agent.setSeePlayerOutlines(!agent.canSeePlayerOutlines());
                        return 0;
                    }).build();
            LiteralCommandNode<CommandSource> level = literal("level")
                    .then(argument("value", IntegerArgumentType.integer(-1, Short.MAX_VALUE))
                            .requires(src -> src.getEntity() instanceof ForgePlayer)
                            .executes(this.vanish::setVanishLevel)).build();
            LiteralCommandNode<CommandSource> view = literal("view")
                    .then(argument("value", IntegerArgumentType.integer(-1, Short.MAX_VALUE))
                            .requires(src -> src.getEntity() instanceof ForgePlayer)
                            .executes(this.vanish::setViewLevel)).build();
            vanish.addChild(view);
            vanish.addChild(level);
            vanish.addChild(add);
            vanish.addChild(chams);
            vanish.addChild(remove);
            event.getDispatcher().getRoot().addChild(vanish);
        }
    }
}
