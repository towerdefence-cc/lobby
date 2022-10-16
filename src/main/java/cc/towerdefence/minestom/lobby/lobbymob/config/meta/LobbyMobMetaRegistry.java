package cc.towerdefence.minestom.lobby.lobbymob.config.meta;

import com.google.gson.JsonObject;
import net.minestom.server.entity.EntityType;

import java.util.Map;
import java.util.function.Function;

public class LobbyMobMetaModifiers {
    private final Map<EntityType, Function<JsonObject, LobbyMobMetaModifier>> metaModifiers;
}
