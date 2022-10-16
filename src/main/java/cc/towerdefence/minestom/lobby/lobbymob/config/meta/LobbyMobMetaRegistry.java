package cc.towerdefence.minestom.lobby.lobbymob.config.meta;

import cc.towerdefence.minestom.lobby.lobbymob.config.meta.animal.tameable.ParrotLobbyMobMeta;
import com.google.gson.JsonObject;
import net.minestom.server.entity.EntityType;

import java.util.Map;
import java.util.function.Function;

public class LobbyMobMetaRegistry {
    private static final Map<EntityType, Function<JsonObject, LobbyMobMeta>> metaModifiers = Map.ofEntries(
            Map.entry(EntityType.PARROT, ParrotLobbyMobMeta::new)
    );

    public static LobbyMobMeta parse(EntityType entityType, JsonObject json) {
        return metaModifiers.get(entityType).apply(json);
    }
}
