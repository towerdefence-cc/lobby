package cc.towerdefence.minestom.lobby.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LobbyUser {
    private final @NotNull UUID uuid;

    private final @NotNull EasterEggData easterEggData;

    public LobbyUser(@NotNull UUID uuid) {
        this.uuid = uuid;
        this.easterEggData = new EasterEggData();
    }
}
