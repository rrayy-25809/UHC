package com.aqua.uhc;

import org.bukkit.World;

/**
 * @deprecated {@link GameEventListener}를 사용하세요. 호환성을 위해 유지됨.
 */
@Deprecated
public class gameEvent extends GameEventListener {
    public gameEvent(World gameWorld) {
        // 기존 생성자 호환: plugin 없이 호출되던 경우 Bukkit 플러그인 조회
        super(org.bukkit.Bukkit.getPluginManager().getPlugin("UHC") instanceof UHC uhc ? uhc : null, gameWorld);
    }
}
