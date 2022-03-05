import microqueue.VoidChunkGenerator;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestServer.class);

    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();
        OptifineSupport.enable();
        MojangAuth.init();

        MinecraftServer.getSchedulerManager().buildShutdownTask(TestServer::shutdown);

        // start of microqueue code
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        instanceContainer.setTime(18_000);
        instanceContainer.setTimeRate(0);
        instanceContainer.setTimeUpdate(null);
        instanceContainer.setChunkGenerator(new VoidChunkGenerator());
        instanceContainer.enableAutoChunkLoad(true);

        instanceContainer.setBlock(0, 62, 0, Block.BEDROCK);

        Pos spawnPoint = new Pos(0.5, 63.0, 0.5);

        eventHandler.addListener(PlayerLoginEvent.class, event -> {
                Player player = event.getPlayer();
                player.setFlying(true);
                player.setAutoViewable(false);
                player.setRespawnPoint(spawnPoint);

                event.setSpawningInstance(instanceContainer);
            })
            .addListener(PlayerSpawnEvent.class, event -> {
                Player player = event.getPlayer();

                player.setGameMode(GameMode.ADVENTURE);
                player.setEnableRespawnScreen(false);

                player.setGravity(0.0, 0.0);
                player.setNoGravity(true);

                player.addEffect(new Potion(PotionEffect.BLINDNESS, Byte.MAX_VALUE, Integer.MAX_VALUE));
                player.addEffect(new Potion(PotionEffect.INVISIBILITY, Byte.MAX_VALUE, Integer.MAX_VALUE));
            })
            .addListener(PlayerMoveEvent.class, event -> {
                event.setCancelled(true);
            });
        // end of microqueue code

        String ip = System.getenv("minestom.address");
        int port = Integer.parseInt(System.getenv("minestom.port"));
        LOGGER.info("Creating server with IP {}:{}", ip, port);
        server.start(ip, port);
    }

    private static void shutdown() {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.kick(Component.text("Server shutting down")));
    }
}
