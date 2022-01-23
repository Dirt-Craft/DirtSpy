package net.dirtcraft.dirtvanish;

import net.dirtcraft.dirtcommons.config.NBTConfig;
import net.dirtcraft.dirtcommons.core.api.ForgePlayer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.nio.file.Path;

public class VanishConfig extends NBTConfig<VanishConfig.Settings> {

    public VanishConfig(Path folder) {
        super(folder, Settings::new);
    }

    @Override
    protected Settings load(CompoundNBT compoundNBT) {
        boolean chams = compoundNBT.getBoolean("chams");
        short view = compoundNBT.getShort("view");
        short show = compoundNBT.getShort("show");
        return new Settings(view, show, chams);
    }

    @Override
    protected void save(CompoundNBT compoundNBT, Settings settings) {
        compoundNBT.putShort("view", settings.viewAccess);
        compoundNBT.putShort("show", settings.vanishLevel);
        compoundNBT.putBoolean("chams", settings.thermals);
    }

    @Override
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        super.onLogin(event);
        ForgePlayer player = (ForgePlayer) event.getPlayer();
        Settings settings = get(event.getPlayer());
        if (settings == null) return;
        player.setVanishLevel((short) Math.min(settings.vanishLevel, player.getMetaOrDefault(Permissions.META_LEVEL_MAX, Integer::valueOf, 0)));
        player.setVanishViewLevel((short) Math.min(settings.viewAccess, player.getMetaOrDefault(Permissions.META_VIEW_MAX, Integer::valueOf, 0)));
        player.setSeePlayerOutlines(settings.thermals & player.hasPermission(Permissions.THERMALS));
    }

    static class Settings {
        private short viewAccess;
        private short vanishLevel;
        private boolean thermals;

        public Settings(){

        }

        private Settings(short view, short show, boolean thermals) {
            this.viewAccess = view;
            this.vanishLevel = show;
            this.thermals = thermals;
        }
    }
}
