package cc.towerdefence.minestom.lobby.menus;

import cc.towerdefence.api.service.PlayerTrackerGrpc;
import cc.towerdefence.api.service.PlayerTrackerProto;
import cc.towerdefence.api.service.velocity.VelocityServerGrpc;
import cc.towerdefence.api.service.velocity.VelocityServerProto;
import cc.towerdefence.api.utils.utils.FunctionalFutureCallback;
import cc.towerdefence.minestom.lobby.LobbyModule;
import cc.towerdefence.minestom.lobby.cache.LobbyUserCache;
import cc.towerdefence.minestom.lobby.model.LobbyUser;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ServerSelectorMenu {
    private static final ItemStack HOTBAR_ITEM = ItemStack.builder(Material.COMPASS)
            .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Server Selector").decoration(TextDecoration.ITALIC, false))
            .build();

    private static final ItemStack TOWER_DEFENCE_ITEM = ItemStack.builder(Material.STONE_BRICKS)
            .displayName(MiniMessage.miniMessage().deserialize("<color:#c98fff>Tower Defence").decoration(TextDecoration.ITALIC, false))
            .build();

    private static final Component INVENTORY_TITLE = MiniMessage.miniMessage().deserialize("<dark_purple>Server Selector");

    private final PlayerTrackerGrpc.PlayerTrackerFutureStub playerTrackerService;
    private final LoadingCache<String, VelocityServerGrpc.VelocityServerFutureStub> velocityServiceCache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(key -> {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(key, 9090)
                        .usePlaintext()
                        .build();

                return VelocityServerGrpc.newFutureStub(channel);
            });

    private final LobbyUserCache lobbyUserCache;

    public ServerSelectorMenu(LobbyModule module) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("player-tracker.towerdefence.svc", 9090)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();
        this.playerTrackerService = PlayerTrackerGrpc.newFutureStub(managedChannel);

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

                    if (event.getClickedItem() == TOWER_DEFENCE_ITEM) {
                        player.closeInventory();
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<light_purple>Connecting you to <color:#c98fff>tower defence<light_purple>..."));
                        this.sendToTowerDefence(player);
                    } else if (event.getSlot() == 8) { // quick join button
                        LobbyUser lobbyUser = this.lobbyUserCache.getUser(player.getUuid());
                        lobbyUser.setQuickJoin(!lobbyUser.isQuickJoin());
                        inventory.setItemStack(8, this.createQuickJoinItem(lobbyUser));
                    }
                });
    }

    private Inventory createInventory(@NotNull Player player) {
        LobbyUser lobbyUser = this.lobbyUserCache.getUser(player.getUuid());

        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, INVENTORY_TITLE);
        inventory.setItemStack(4, TOWER_DEFENCE_ITEM);
        inventory.setItemStack(8, this.createQuickJoinItem(lobbyUser));

        return inventory;
    }

    private ItemStack createQuickJoinItem(@NotNull LobbyUser lobbyUser) {
        return ItemStack.builder(lobbyUser.isQuickJoin() ? Material.GREEN_WOOL : Material.RED_WOOL)
                .displayName(MiniMessage.miniMessage().deserialize("<light_purple>Quick Join: " + (lobbyUser.isQuickJoin() ? "<green>Enabled" : "<red>Disabled")).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<light_purple>Allows you to join ongoing games").decoration(TextDecoration.ITALIC, false),
                        MiniMessage.miniMessage().deserialize("<light_purple>with space for more players.").decoration(TextDecoration.ITALIC, false)
                )
                .build();
    }

    private void sendToTowerDefence(Player player) {
        LobbyUser lobbyUser = this.lobbyUserCache.getUser(player.getUuid());

        ListenableFuture<PlayerTrackerProto.GetPlayerServerResponse> serverResponseFuture = this.playerTrackerService.getPlayerServer(
                PlayerTrackerProto.PlayerRequest.newBuilder()
                        .setPlayerId(String.valueOf(player.getUuid())).build()
        );

        Futures.addCallback(serverResponseFuture, FunctionalFutureCallback.create(
                serverResponse -> {
                    String serverId = serverResponse.getServer().getServerId();
                    VelocityServerGrpc.VelocityServerFutureStub velocityService = this.velocityServiceCache.get(serverId);

                    ListenableFuture<Empty> swapServerResponse = velocityService.swapTowerDefence(VelocityServerProto.TowerDefenceSwapRequest.newBuilder()
                            .setPlayerId(player.getUuid().toString())
                            .setQuickJoin(lobbyUser.isQuickJoin()).build());
                    this.handleSwapServerResponse(swapServerResponse, player);
                },
                throwable -> {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Failed to identify your current server."));
                }
        ), ForkJoinPool.commonPool());
    }

    private void handleSwapServerResponse(ListenableFuture<Empty> listenableFuture, Player player) {
        Futures.addCallback(listenableFuture, FunctionalFutureCallback.create(
                empty -> {},
                throwable -> {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Failed to connect to Tower Defence."));
                }
        ), ForkJoinPool.commonPool());
    }
}
