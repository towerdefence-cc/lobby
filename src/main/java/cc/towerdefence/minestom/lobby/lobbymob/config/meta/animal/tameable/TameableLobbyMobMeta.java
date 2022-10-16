package cc.towerdefence.minestom.lobby.lobbymob.config.meta.animal.tameable;

import cc.towerdefence.minestom.lobby.lobbymob.config.meta.AgeableLobbyMobMeta;
import com.google.gson.JsonObject;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.animal.tameable.TameableAnimalMeta;

import java.util.UUID;

public class TameableLobbyMobMeta extends AgeableLobbyMobMeta {
    private final Boolean sitting;
    private final Boolean tamed;
    private final UUID owner;

    public TameableLobbyMobMeta(JsonObject json) {
        super(json);

        this.sitting = json.has("sitting") ? json.get("sitting").getAsBoolean() : null;
        this.tamed = json.has("tamed") ? json.get("tamed").getAsBoolean() : null;
        this.owner = json.has("owner") ? UUID.fromString(json.get("owner").getAsString()) : null;
    }

    @Override
    public void apply(EntityMeta entityMeta) {
        TameableAnimalMeta tameableAnimalMeta = (TameableAnimalMeta) entityMeta;
        if (this.sitting != null) tameableAnimalMeta.setSitting(this.sitting);
        if (this.tamed != null) tameableAnimalMeta.setTamed(this.tamed);
        if (this.owner != null) tameableAnimalMeta.setOwner(this.owner);

        super.apply(entityMeta);
    }
}
