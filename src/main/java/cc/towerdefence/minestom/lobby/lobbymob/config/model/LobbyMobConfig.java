package cc.towerdefence.minestom.lobby.lobbymob.config.model;

import cc.towerdefence.minestom.lobby.lobbymob.config.meta.LobbyMobMeta;
import cc.towerdefence.minestom.lobby.lobbymob.config.meta.LobbyMobMetaRegistry;
import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public record LobbyMobConfig(@NotNull EntityType type, LobbyMobMeta meta, @NotNull Pos pos) {

    public static @NotNull LobbyMobConfig parse(@NotNull JsonObject json) {
        EntityType entityType = EntityType.fromNamespaceId(json.get("type").getAsString());
        LobbyMobMeta meta = LobbyMobMetaRegistry.parse(entityType, json.has("meta") ? json.get("meta").getAsJsonObject() : new JsonObject());
        return new LobbyMobConfig(
                entityType,
                meta,
                ConfigPos.parse(json.get("pos").getAsJsonObject())
        );
    }
}
