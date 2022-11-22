package cc.towerdefence.minestom.lobby.menus;

import cc.towerdefence.api.service.PlayerTrackerGrpc;
import cc.towerdefence.api.service.PlayerTrackerProto;
import cc.towerdefence.api.service.PlayerTransporterGrpc;
import cc.towerdefence.api.service.PlayerTransporterProto;
import cc.towerdefence.api.service.velocity.VelocityServerGrpc;
import cc.towerdefence.api.utils.GrpcStubCollection;
import cc.towerdefence.api.utils.utils.FunctionalFutureCallback;
import cc.towerdefence.minestom.lobby.LobbyModule;
import cc.towerdefence.minestom.lobby.cache.LobbyUserCache;
import cc.towerdefence.minestom.lobby.model.LobbyUser;
import cc.towerdefence.minestom.module.kubernetes.KubernetesModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.ExecutionType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ServerSelectorMenu {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSelectorMenu.class);
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final ItemStack HOTBAR_ITEM = ItemStack.builder(Material.COMPASS)
            .displayName(MINI_MESSAGE.deserialize("<light_purple>Server Selector").decoration(TextDecoration.ITALIC, false))
            .build();

    private static final Component INVENTORY_TITLE = MINI_MESSAGE.deserialize("<dark_purple>Server Selector");

    private final LoadingCache<String, VelocityServerGrpc.VelocityServerFutureStub> velocityServiceCache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(key -> {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(key, 9090)
                        .usePlaintext()
                        .build();

                return VelocityServerGrpc.newFutureStub(channel);
            });

    private final PlayerTrackerGrpc.PlayerTrackerFutureStub playerTrackerService;
    private final PlayerTransporterGrpc.PlayerTransporterFutureStub playerTransporterService;
    private final CoreV1Api kubernetesClient;

    private final LobbyUserCache lobbyUserCache;

    private ItemStack towerDefenceItem = ItemStack.builder(Material.STONE_BRICKS)
            .displayName(MINI_MESSAGE.deserialize("<color:#c98fff>Tower Defence").decoration(TextDecoration.ITALIC, false))
            .build();

    public ServerSelectorMenu(LobbyModule module, KubernetesModule kubernetesModule) {
        this.playerTrackerService = GrpcStubCollection.getPlayerTrackerService().orElse(null);
        this.playerTransporterService = GrpcStubCollection.getPlayerTransporterService().orElse(null);
        this.kubernetesClient = new CoreV1Api(kubernetesModule.getApiClient());
        this.lobbyUserCache = module.getLobbyUserCache();

        module.getEventNode()
                .addListener(PlayerSpawnEvent.class, event -> event.getPlayer().getInventory().setItemStack(0, HOTBAR_ITEM))
                .addListener(PlayerUseItemEvent.class, event -> {
                    if (event.getHand() == Player.Hand.OFF) return;

                    Player player = event.getPlayer();
                    player.openInventory(this.createInventory(player));
                })
                .addListener(InventoryPreClickEvent.class, event -> {
                    if (event.getClickType() == ClickType.DOUBLE_CLICK) return;

                    Inventory inventory = event.getInventory();
                    if (inventory == null || inventory.getTitle() != INVENTORY_TITLE) return;

                    Player player = event.getPlayer();
                    event.setCancelled(true);

                    if (event.getClickedItem() == this.towerDefenceItem) {
                        player.closeInventory();
                        player.sendMessage(MINI_MESSAGE.deserialize("<light_purple>Connecting you to <color:#c98fff>tower defence<light_purple>..."));
                        this.sendToTowerDefence(player);
                    } else if (event.getSlot() == 8) { // quick join button
                        LobbyUser lobbyUser = this.lobbyUserCache.getUser(player.getUuid());
                        lobbyUser.setQuickJoin(!lobbyUser.isQuickJoin());
                        inventory.setItemStack(8, this.createQuickJoinItem(lobbyUser));
                    }
                });

        MinecraftServer.getSchedulerManager().buildTask(this::updateItems)
                .executionType(ExecutionType.ASYNC)
                .repeat(5, ChronoUnit.SECONDS)
                .schedule();
    }

    private void updateItems() {
        this.towerDefenceItem = ItemStack.builder(Material.STONE_BRICKS)
                .displayName(MINI_MESSAGE.deserialize("<color:#c98fff>Tower Defence").decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.empty(),
                        MINI_MESSAGE.deserialize("<light_purple>In Game: <dark_purple><player_count>",
                                Placeholder.unparsed("player_count", String.valueOf(this.getPlayerCount("tower-defence-game")))
                        ).decoration(TextDecoration.ITALIC, false)
                )
                .build();
    }

    private Inventory createInventory(@NotNull Player player) {
        LobbyUser lobbyUser = this.lobbyUserCache.getUser(player.getUuid());

        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, INVENTORY_TITLE);
        inventory.setItemStack(4, this.towerDefenceItem);
        inventory.setItemStack(8, this.createQuickJoinItem(lobbyUser));

        return inventory;
    }

    private ItemStack createQuickJoinItem(@NotNull LobbyUser lobbyUser) {
        return ItemStack.builder(lobbyUser.isQuickJoin() ? Material.GREEN_WOOL : Material.RED_WOOL)
                .displayName(MINI_MESSAGE.deserialize("<light_purple>Quick Join: " + (lobbyUser.isQuickJoin() ? "<green>Enabled" : "<red>Disabled")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.empty(),
                        MINI_MESSAGE.deserialize("<light_purple>Allows you to join ongoing games").decoration(TextDecoration.ITALIC, false),
                        MINI_MESSAGE.deserialize("<light_purple>with space for more players.").decoration(TextDecoration.ITALIC, false)
                )
                .build();
    }

    // todo this can be optimised to cache earlier - by id - instead of by ip. This will mean less queries to the k8s api
    private void sendToTowerDefence(@NotNull Player player) {
        LobbyUser lobbyUser = this.lobbyUserCache.getUser(player.getUuid());

        ListenableFuture<Empty> serverMoveFuture = this.playerTransporterService.towerDefenceGameMovePlayer(
                PlayerTransporterProto.TowerDefenceGameMoveRequest.newBuilder()
                        .setFastJoin(lobbyUser.isQuickJoin())
                        .addPlayerIds(player.getUuid().toString())
                        .build());

        Futures.addCallback(serverMoveFuture, FunctionalFutureCallback.create(
                unused -> {

                },
                throwable -> {
                    player.sendMessage(MINI_MESSAGE.deserialize("<red>Failed to find your current server."));
                    LOGGER.error("Failed to send player ({}, {}) to tower-defence-game: {}", player.getUsername(), player.getUuid(), throwable);
                }
        ), ForkJoinPool.commonPool());


//        this.getProxyIpForPlayer(player.getUuid(), optionalIp -> {
//            if (optionalIp.isEmpty()) {
//                player.sendMessage(MINI_MESSAGE.deserialize("<red>Failed to find your current server."));
//                return;
//            }
//
//            String proxyIp = optionalIp.get();
//            VelocityServerGrpc.VelocityServerFutureStub velocityService = this.velocityServiceCache.get(proxyIp);
//
//            ListenableFuture<Empty> swapServerResponse = velocityService.swapTowerDefence(VelocityServerProto.TowerDefenceSwapRequest.newBuilder()
//                    .setPlayerId(player.getUuid().toString())
//                    .setQuickJoin(lobbyUser.isQuickJoin()).build());
//            this.handleSwapServerResponse(swapServerResponse, player);
//        });
    }

    private void handleSwapServerResponse(ListenableFuture<Empty> listenableFuture, Player player) {
        Futures.addCallback(listenableFuture, FunctionalFutureCallback.create(
                empty -> {
                },
                throwable -> {
                    throwable.printStackTrace();
                    player.sendMessage(MINI_MESSAGE.deserialize("<red>Failed to connect to Tower Defence."));
                }
        ), ForkJoinPool.commonPool());
    }

    public void getProxyIpForPlayer(UUID playerId, Consumer<Optional<String>> callback) {
        ListenableFuture<PlayerTrackerProto.GetPlayerServerResponse> serverResponseFuture = this.playerTrackerService.getPlayerServer(
                PlayerTrackerProto.PlayerRequest.newBuilder()
                        .setPlayerId(String.valueOf(playerId)).build()
        );

        Futures.addCallback(serverResponseFuture, FunctionalFutureCallback.create(
                serverResponse -> {
                    if (!serverResponse.hasServer()) {
                        callback.accept(Optional.empty());
                        return;
                    }
                    String proxyId = serverResponse.getServer().getProxyId();

                    try {
                        V1Pod pod = this.kubernetesClient.readNamespacedPod(proxyId, "towerdefence", null);
                        callback.accept(Optional.ofNullable(pod.getStatus().getPodIP()));
                    } catch (ApiException e) {
                        LOGGER.error("Failed to get pod for proxy id {}:\nK8s Error: ({}) {}\n{}", proxyId, e.getCode(), e.getResponseBody(), e);
                        callback.accept(Optional.empty());
                    }
                },
                throwable -> callback.accept(Optional.empty())
        ), ForkJoinPool.commonPool());
    }

    private int getPlayerCount(@NotNull String serverType) {
        try {
            if (this.playerTrackerService == null) return -1;
            return this.playerTrackerService.getServerTypePlayerCount(
                    PlayerTrackerProto.ServerTypeRequest.newBuilder().setServerType(serverType).build()
            ).get().getPlayerCount();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Failed to get player count for server type {}:\n{}", serverType, e);

            return -1;
        }
    }
}
