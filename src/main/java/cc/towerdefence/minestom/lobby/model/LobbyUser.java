package cc.towerdefence.minestom.lobby.model;

import cc.towerdefence.minestom.lobby.LobbyExtension;
import cc.towerdefence.openmatch.frontend.client.model.Ticket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
@Getter
@Setter
public class LobbyUser {
    private final @NotNull LobbyExtension extension;
    private final @NotNull Player player;
    private @Nullable Ticket ticket;
}
