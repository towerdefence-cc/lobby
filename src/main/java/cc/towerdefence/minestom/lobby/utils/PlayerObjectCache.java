package cc.towerdefence.minestom.lobby.utils;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerObjectCache<T> {
    private final LoadingCache<Player, T> cache;

    public PlayerObjectCache(CacheLoader<Player, T> loader) {
        this.cache = Caffeine.newBuilder().weakKeys().build(loader);
    }

    public @NotNull T get(@NotNull Player player) {
        return this.cache.get(player);
    }
}
