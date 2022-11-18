package cc.towerdefence.minestom.lobby.cache;

import cc.towerdefence.minestom.lobby.LobbyModule;
import cc.towerdefence.minestom.lobby.model.LobbyUser;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyUserCache {
    private final Map<UUID, LobbyUser> lobbyUsers = new ConcurrentHashMap<>();

    public LobbyUserCache(LobbyModule module) {
        module.getEventNode().addListener(PlayerLoginEvent.class, event -> this.load(event.getPlayer().getUuid()));
        module.getEventNode().addListener(PlayerDisconnectEvent.class, event -> this.invalidate(event.getPlayer().getUuid()));

        MinecraftServer.getSchedulerManager().buildShutdownTask(this::invalidateAll);
    }

    public @NotNull LobbyUser getUser(UUID uuid) {
        LobbyUser user = this.lobbyUsers.get(uuid);
        if (user == null) {
            Optional<LobbyUser> optionalUser = this.load(uuid);
            if (optionalUser.isEmpty())
                user = this.create(uuid);
            else
                user = optionalUser.get();
            this.lobbyUsers.put(uuid, user);
        }
        return user;
    }

    private LobbyUser create(UUID uuid) {
        LobbyUser user = new LobbyUser(uuid);
//        this.userRepository.save(uuid, user); todo
        return user;
    }

    private Optional<LobbyUser> load(UUID uuid) {
//        return this.userRepository.findById(uuid); todo
        return Optional.of(new LobbyUser(uuid));
    }

    private void invalidate(UUID uuid) {
        LobbyUser user = this.lobbyUsers.remove(uuid);
//        if (user != null) todo
//            this.userRepository.save(uuid, user);
    }

    public void invalidateAll() {
        for (Map.Entry<UUID, LobbyUser> entry : this.lobbyUsers.entrySet()) {
//            this.userRepository.save(entry.getKey(), entry.getValue()); todo
        }
        this.lobbyUsers.clear();
    }
}
