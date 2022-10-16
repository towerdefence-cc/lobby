package cc.towerdefence.minestom.lobby.eastereggs;

import cc.towerdefence.minestom.lobby.LobbyExtension;
import cc.towerdefence.minestom.lobby.cache.LobbyUserCache;
import cc.towerdefence.minestom.lobby.model.LobbyUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.tameable.ParrotMeta;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerStartFlyingEvent;
import net.minestom.server.event.player.PlayerStartFlyingWithElytraEvent;
import net.minestom.server.event.player.PlayerStopFlyingEvent;
import net.minestom.server.event.player.PlayerStopFlyingWithElytraEvent;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;

import java.util.Map;

public class ParkourParrotEasterEgg {
    private final LobbyUserCache lobbyUserCache;

    public ParkourParrotEasterEgg(@NotNull LobbyExtension extension) {
        this.lobbyUserCache = extension.getLobbyUserCache();

        this.activationListeners(extension.getEventNode());
        this.rewardListeners(extension.getEventNode());
    }

    private void rewardListeners(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            Pos pos = player.getPosition();
            if (!this.isInArea(pos)) return;

            LobbyUser lobbyUser = this.lobbyUserCache.getUser(player.getUuid());
            if (lobbyUser.getEasterEggData().isParkourParrot()) return;

            this.applyParrot(player);
            lobbyUser.getEasterEggData().setParkourParrot(true);
            event.getPlayer().sendMessage(Component.text()
                    .append(Component.text("You found the parkour parrot easter egg!", NamedTextColor.GREEN))
                    .append(Component.newline())
                    .append(Component.text("You now have a parrot on your shoulder :)", NamedTextColor.GREEN)));
        });
    }

    // reapply the parrot when a player stops flying
    // Also remove the parrot when the player starts flying - this just ensures the server/client are in sync
    private void activationListeners(@NotNull EventNode<Event> eventNode) {
        eventNode
                // removals
                .addListener(PlayerStartFlyingEvent.class, event -> event.getPlayer().getEntityMeta().setLeftShoulderEntityData(NBT.Compound(Map.of())))
                .addListener(PlayerStartFlyingWithElytraEvent.class, event -> event.getPlayer().getEntityMeta().setLeftShoulderEntityData(NBT.Compound(Map.of())))

                // additions
                .addListener(PlayerSpawnEvent.class, event -> this.applyParrotIfEligible(event.getPlayer()))
                .addListener(PlayerStopFlyingEvent.class, event -> this.applyParrotIfEligible(event.getPlayer()))
                .addListener(PlayerStopFlyingWithElytraEvent.class, event -> this.applyParrotIfEligible(event.getPlayer()));
    }

    private void applyParrotIfEligible(@NotNull Player player) {
        if (this.lobbyUserCache.getUser(player.getUuid()).getEasterEggData().isParkourParrot())
            this.applyParrot(player);
    }

    // The parrot is removed if the player starts flying (and with an elytra)
    private void applyParrot(@NotNull Player player) {
        new Entity(EntityType.PARROT).getMetadataPacket();

        NBT parrotNbt = NBT.Compound(
                Map.of(
                        "id", NBT.String("minecraft:parrot"),
                        "Variant", NBT.Int(ParrotMeta.Color.RED_BLUE.ordinal()) // default
                )
        );
        player.getEntityMeta().setLeftShoulderEntityData(parrotNbt);
    }

    private boolean isInArea(Pos pos) {
        return pos.x() >= -17 && pos.x() <= -14 &&
                pos.y() >= 108 && pos.y() <= 109 &&
                pos.z() >= 26 && pos.z() <= 29;
    }
}
