package cc.towerdefence.minestom.lobby.lobbymob.config.meta;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.Nullable;

public class LobbyMobMeta {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final @Nullable Boolean onFire;
    private final @Nullable Boolean sneaking;
    private final @Nullable Boolean sprinting;
    private final @Nullable Boolean swimming;
    private final @Nullable Boolean invisible;
    private final @Nullable Boolean glowingEffect;
    private final @Nullable Boolean flyingWithElytra;
    private final @Nullable Integer airTicks;
    private final @Nullable Component customName;
    private final @Nullable Boolean customNameVisible;
    private final @Nullable Boolean silent;
    private final @Nullable Boolean noGravity;
    private final @Nullable Entity.Pose pose;
    private final @Nullable Integer tickFrozen;

    public LobbyMobMeta(JsonObject json) {
        this.onFire = json.has("onFire") ? json.get("onFire").getAsBoolean() : null;
        this.sneaking = json.has("sneaking") ? json.get("sneaking").getAsBoolean() : null;
        this.sprinting = json.has("sprinting") ? json.get("sprinting").getAsBoolean() : null;
        this.swimming = json.has("swimming") ? json.get("swimming").getAsBoolean() : null;
        this.invisible = json.has("invisible") ? json.get("invisible").getAsBoolean() : null;
        this.glowingEffect = json.has("glowingEffect") ? json.get("glowingEffect").getAsBoolean() : null;
        this.flyingWithElytra = json.has("flyingWithElytra") ? json.get("flyingWithElytra").getAsBoolean() : null;
        this.airTicks = json.has("airTicks") ? json.get("airTicks").getAsInt() : null;
        this.customName = json.has("customName") ? MINI_MESSAGE.deserialize(json.get("customName").getAsString()) : null;
        this.customNameVisible = json.has("customNameVisible") ? json.get("customNameVisible").getAsBoolean() : null;
        this.silent = json.has("silent") ? json.get("silent").getAsBoolean() : null;
        this.noGravity = json.has("noGravity") ? json.get("noGravity").getAsBoolean() : null;
        this.pose = json.has("pose") ? Entity.Pose.valueOf(json.get("pose").getAsString()) : null;
        this.tickFrozen = json.has("tickFrozen") ? json.get("tickFrozen").getAsInt() : null;
    }

    public void apply(EntityMeta entityMeta) {
        if (this.onFire != null) entityMeta.setOnFire(this.onFire);
        if (this.sneaking != null) entityMeta.setSneaking(this.sneaking);
        if (this.sprinting != null) entityMeta.setSprinting(this.sprinting);
        if (this.swimming != null) entityMeta.setSwimming(this.swimming);
        if (this.invisible != null) entityMeta.setInvisible(this.invisible);
        if (this.glowingEffect != null) entityMeta.setHasGlowingEffect(this.glowingEffect);
        if (this.flyingWithElytra != null) entityMeta.setFlyingWithElytra(this.flyingWithElytra);
        if (this.airTicks != null) entityMeta.setAirTicks(this.airTicks);
        if (this.customName != null) entityMeta.setCustomName(this.customName);
        if (this.customNameVisible != null) entityMeta.setCustomNameVisible(this.customNameVisible);
        if (this.silent != null) entityMeta.setSilent(this.silent);
        if (this.noGravity != null) entityMeta.setHasNoGravity(this.noGravity);
        if (this.pose != null) entityMeta.setPose(this.pose);
        if (this.tickFrozen != null) entityMeta.setTickFrozen(this.tickFrozen);
    }
}
