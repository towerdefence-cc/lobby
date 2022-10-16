package cc.towerdefence.minestom.lobby.lobbymob.config.model;

import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public class ConfigPos {

    public static @NotNull Pos parse(@NotNull JsonObject json, float defaultYaw, float defaultPitch) {
        return new Pos(
                json.get("x").getAsDouble(),
                json.get("y").getAsDouble(),
                json.get("z").getAsDouble(),
                json.has("yaw") ? json.get("yaw").getAsFloat() : defaultYaw,
                json.has("pitch") ? json.get("pitch").getAsFloat() : defaultPitch
        );
    }

    public static @NotNull Pos parse(@NotNull JsonObject json) {
        return parse(json, 0, 0);
    }
}
