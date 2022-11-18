package cc.towerdefence.minestom.lobby.lobbymob;

import cc.towerdefence.minestom.lobby.LobbyModule;
import cc.towerdefence.minestom.lobby.lobbymob.config.LobbyMobRegistry;
import cc.towerdefence.minestom.lobby.lobbymob.config.model.LobbyMobConfig;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public class LobbyMobManager {
    private final @NotNull Instance instance;

    public LobbyMobManager(@NotNull LobbyModule module) {
        this.instance = module.getLobbyInstance();

        LobbyMobRegistry lobbyMobRegistry = new LobbyMobRegistry();

        for (LobbyMobConfig lobbyMobConfig : lobbyMobRegistry.getMobs()) this.createMob(lobbyMobConfig);
    }

    private void createMob(@NotNull LobbyMobConfig configMob) {
        Entity entity = new Entity(configMob.type());
        configMob.meta().apply(entity.getEntityMeta());

        entity.setInstance(this.instance, configMob.pos());
    }
}
