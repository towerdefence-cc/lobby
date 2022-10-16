package cc.towerdefence.minestom.lobby.lobbymob.config.meta.animal.tameable;

import com.google.gson.JsonObject;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.animal.tameable.ParrotMeta;

public class ParrotLobbyMobMeta extends TameableLobbyMobMeta {
    private final ParrotMeta.Color color;

    public ParrotLobbyMobMeta(JsonObject json) {
        super(json);

        this.color = json.has("color") ? ParrotMeta.Color.valueOf(json.get("color").getAsString()) : null;
    }

    @Override
    public void apply(EntityMeta entityMeta) {
        ParrotMeta parrotMeta = (ParrotMeta) entityMeta;
        if (this.color != null) parrotMeta.setColor(this.color);

        super.apply(entityMeta);
    }
}
