package com.janboerman.invsee.spigot.impl_1_15_R1;

import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.WorldNBTStorage;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;

public class FakeCraftPlayer extends CraftPlayer {
    public FakeCraftPlayer(CraftServer server, EntityPlayer entity) {
        super(server, entity);
    }

    @Override
    public void setExtraData(NBTTagCompound tag) {
        super.setExtraData(tag);

        NBTTagCompound freshlyLoaded = loadPlayerTag();
        NBTTagCompound loadedBukkit = freshlyLoaded.getCompound("bukkit");
        NBTTagCompound loadedPaper = freshlyLoaded.getCompound("Paper");

        if (tag.hasKeyOfType("bukkit", 10) && loadedBukkit != null && !loadedBukkit.isEmpty())
            tag.getCompound("bukkit").setLong("lastPlayed", loadedBukkit.getLong("lastPlayed"));
        if (tag.hasKeyOfType("Paper", 10) && loadedPaper != null && !loadedPaper.isEmpty())
            tag.getCompound("Paper").setLong("lastSeen", loadedPaper.getLong("LastSeen"));
    }

    private NBTTagCompound loadPlayerTag() {
        return ((WorldNBTStorage) server.getHandle().playerFileData).getPlayerData(getUniqueId().toString());
    }
}
