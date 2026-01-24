package com.aqua.uhc;

import java.util.Objects;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

public class UHC extends JavaPlugin {
    public MultiverseCoreApi coreApi;

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        Plugin mv = pm.getPlugin("Multiverse-Core");
        if (mv == null || !mv.isEnabled()) {
            getLogger().severe("Multiverse-Core not enabled!");
            pm.registerEvents(new Listener() {
                @EventHandler
                public void onPluginEnable(PluginEnableEvent event) {
                    if (event.getPlugin().getName().equals("Multiverse-Core")) {
                        hookMultiverse();
                    }
                }
            }, this);
        } else {
            hookMultiverse();
        }
    }

    private void hookMultiverse() {
        this.coreApi = MultiverseCoreApi.get();
        Objects.requireNonNull(getCommand("test")).setExecutor(new command(this));
        getLogger().info("UHC 플러그인이 활성화 되었습니다!");
    }

    @Override
    public void onDisable() {
        getLogger().info("UHC 플러그인이 비활성화 되었습니다!");
    }
}
