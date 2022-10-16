package cc.towerdefence.minestom.lobby.lobbymob.config;

import cc.towerdefence.minestom.lobby.LobbyExtension;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class LobbyMobConfig {
    private final Set<cc.towerdefence.minestom.lobby.lobbymob.config.model.LobbyMobConfig> mobs;

    public LobbyMobConfig(LobbyExtension extension) {
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(extension.getPackagedResource("mobs.json")));

        Set<cc.towerdefence.minestom.lobby.lobbymob.config.model.LobbyMobConfig> mobs = new HashSet<>();
        for (JsonElement entityJsonElement : jsonElement.getAsJsonArray()) {
            JsonObject entityJson = entityJsonElement.getAsJsonObject();
            cc.towerdefence.minestom.lobby.lobbymob.config.model.LobbyMobConfig configMob = cc.towerdefence.minestom.lobby.lobbymob.config.model.LobbyMobConfig.parse(entityJson);
            mobs.add(configMob);
        }

        this.mobs = mobs;
    }

    public Set<cc.towerdefence.minestom.lobby.lobbymob.config.model.LobbyMobConfig> getMobs() {
        return mobs;
    }
}
