package cc.towerdefence.minestom.lobby.lobbymob.config;

import cc.towerdefence.minestom.lobby.lobbymob.config.model.LobbyMobConfig;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.SneakyThrows;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class LobbyMobRegistry {
    private final Set<LobbyMobConfig> mobs;

    @SneakyThrows
    public LobbyMobRegistry() {
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(LobbyMobRegistry.class.getClassLoader().getResourceAsStream("mobs.json")));

        Set<LobbyMobConfig> mobs = new HashSet<>();
        for (JsonElement entityJsonElement : jsonElement.getAsJsonArray()) {
            JsonObject entityJson = entityJsonElement.getAsJsonObject();
            LobbyMobConfig configMob = LobbyMobConfig.parse(entityJson);
            mobs.add(configMob);
        }

        this.mobs = mobs;
    }

    public Set<LobbyMobConfig> getMobs() {
        return mobs;
    }
}
