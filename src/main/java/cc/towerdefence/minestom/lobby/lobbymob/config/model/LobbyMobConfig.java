package cc.towerdefence.minestom.lobby.lobbymob.config.model;

import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public record ConfigMob(@NotNull EntityType type, @NotNull Pos pos) {

    public static @NotNull ConfigMob parse(@NotNull JsonObject json) {
        return new ConfigMob(
                EntityType.fromNamespaceId(json.get("type").getAsString()),
                ConfigPos.parse(json.get("pos").getAsJsonObject())
        );
    }
}
