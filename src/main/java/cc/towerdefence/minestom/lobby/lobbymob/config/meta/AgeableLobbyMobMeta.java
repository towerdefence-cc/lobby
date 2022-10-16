package cc.towerdefence.minestom.lobby.lobbymob.config.meta;

import com.google.gson.JsonObject;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.entity.metadata.EntityMeta;

public class AgeableLobbyMobMeta extends LobbyMobMeta {
    private final Boolean baby;

    public AgeableLobbyMobMeta(JsonObject json) {
        super(json);

        this.baby = json.has("baby") ? json.get("baby").getAsBoolean() : null;
    }

    @Override
    public void apply(EntityMeta entityMeta) {
        AgeableMobMeta ageableMobMeta = (AgeableMobMeta) entityMeta;
        if (this.baby != null) ageableMobMeta.setBaby(this.baby);

        super.apply(entityMeta);
    }
}
