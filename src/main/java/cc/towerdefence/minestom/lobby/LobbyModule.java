package cc.towerdefence.minestom.lobby;

import cc.towerdefence.minestom.lobby.blockhandler.SignHandler;
import cc.towerdefence.minestom.lobby.blockhandler.SkullHandler;
import cc.towerdefence.minestom.lobby.cache.LobbyUserCache;
import cc.towerdefence.minestom.lobby.command.SpawnCommand;
import cc.towerdefence.minestom.lobby.eastereggs.ParkourParrotEasterEgg;
import cc.towerdefence.minestom.lobby.lobbymob.LobbyMobManager;
import cc.towerdefence.minestom.lobby.menus.ServerSelectorMenu;
import cc.towerdefence.minestom.module.Module;
import cc.towerdefence.minestom.module.ModuleData;
import cc.towerdefence.minestom.module.ModuleEnvironment;
import cc.towerdefence.minestom.module.kubernetes.KubernetesModule;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ModuleData(name = "lobby", required = true)
public final class LobbyModule extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger(LobbyModule.class);

    public static final Pos SPAWN_POS = new Pos(26.5, 59, 4.5, 180, 0);
    private static final DimensionType DIMENSION_TYPE = DimensionType.builder(NamespaceID.from("towerdefence", "lobby"))
            .ambientLight(0.0f)
            .skylightEnabled(true)
            .fixedTime(6000L)
            .build();

    private Instance lobbyInstance;
    private LobbyUserCache lobbyUserCache;

    public LobbyModule(@NotNull ModuleEnvironment environment) {
        super(environment);
    }

    @Override
    public boolean onLoad() {
        SignHandler.register();
        SkullHandler.register();

        MinecraftServer.getDimensionTypeManager().addDimension(DIMENSION_TYPE);
        this.lobbyInstance = this.createLobbyInstance();
        this.lobbyUserCache = new LobbyUserCache(this);

        this.eventNode.addListener(PlayerLoginEvent.class, event -> {
                    event.setSpawningInstance(this.lobbyInstance);
                    event.getPlayer().setRespawnPoint(SPAWN_POS);
                })
                .addListener(PlayerSpawnEvent.class, event -> event.getPlayer().setGameMode(GameMode.CREATIVE))
                .addListener(PlayerBlockBreakEvent.class, event -> event.setCancelled(true))
                .addListener(PlayerBlockInteractEvent.class, event -> event.setCancelled(true))
                .addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));

        new LobbyMobManager(this);
        new ServerSelectorMenu(this, new CoreV1Api(this.moduleManager.getModule(KubernetesModule.class).getApiClient()));
        new ParkourParrotEasterEgg(this);

        MinecraftServer.getCommandManager().register(new SpawnCommand());

        return true;
    }

    private @NotNull Instance createLobbyInstance() {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer container = instanceManager.createInstanceContainer(DIMENSION_TYPE);
        return container;
    }

    @Override
    public void onUnload() {
        // unused
    }

    public Instance getLobbyInstance() {
        return lobbyInstance;
    }

    public LobbyUserCache getLobbyUserCache() {
        return lobbyUserCache;
    }
}
