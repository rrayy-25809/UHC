package com.aqua.uhc;

import org.bukkit.World;
import org.bukkit.WorldBorder;

public class worldborder {
    private World world;
    private UHC plugin;

    public worldborder(UHC plugin, World world) {
        this.plugin = plugin;
        this.world = world;

        WorldBorder border = this.world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(2000);
    }
}
