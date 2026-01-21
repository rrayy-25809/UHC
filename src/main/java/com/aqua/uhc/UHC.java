package com.aqua.uhc;

import org.bukkit.plugin.java.JavaPlugin;

public final class UHC extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("UHC 플러그인이 활성화 되었습니다!");
    }

    @Override
    public void onDisable() {
        getLogger().info("UHC 플러그인이 비활성화 되었습니다!");
    }
}
