package cc.towerdefence.minestom.lobby.lobbymob;

import cc.towerdefence.minestom.lobby.LobbyExtension;
import cc.towerdefence.minestom.lobby.lobbymob.config.LobbyMobRegistry;
import cc.towerdefence.minestom.lobby.lobbymob.config.model.LobbyMobConfig;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public class LobbyMobManager {
    private final @NotNull Instance instance;

    public LobbyMobManager(@NotNull LobbyExtension extension) {
        this.instance = extension.getLobbyInstance();

        LobbyMobRegistry lobbyMobRegistry = new LobbyMobRegistry(extension);

        for (LobbyMobConfig lobbyMobConfig : lobbyMobRegistry.getMobs()) this.createMob(lobbyMobConfig);
    }

    private void createMob(@NotNull LobbyMobConfig configMob) {
        Entity entity = new Entity(configMob.type());
        configMob.meta().apply(entity.getEntityMeta());

        entity.setInstance(this.instance, configMob.pos());
    }
}
