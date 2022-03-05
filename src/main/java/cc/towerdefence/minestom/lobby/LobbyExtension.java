package cc.towerdefence.minestom.lobby;

import cc.towerdefence.minestom.lobby.menus.QueueMenu;
import cc.towerdefence.minestom.lobby.model.LobbyUser;
import cc.towerdefence.minestom.lobby.utils.PlayerObjectCache;
import cc.towerdefence.openmatch.frontend.client.OpenMatchClient;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.extensions.Extension;
import org.jetbrains.annotations.NotNull;

public class LobbyExtension extends Extension {
    private final @NotNull OpenMatchClient openMatchClient = new OpenMatchClient();
    private final @NotNull PlayerObjectCache<LobbyUser> userCache = new PlayerObjectCache<>(player -> new LobbyUser(this, player));

    @Override
    public void initialize() {
        new QueueMenu(this);


        this.getEventNode()
            .addListener(PlayerChatEvent.class, event -> event.setCancelled(true))
            .addListener(ItemDropEvent.class, event -> event.setCancelled(true))
            .addListener(PlayerSwapItemEvent.class, event -> event.setCancelled(true));
    }

    @Override
    public void terminate() {

    }

    public @NotNull OpenMatchClient getOpenMatchClient() {
        return this.openMatchClient;
    }

    public @NotNull PlayerObjectCache<LobbyUser> getUserCache() {
        return this.userCache;
    }
}
