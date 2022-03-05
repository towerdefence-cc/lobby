package cc.towerdefence.minestom.lobby.menus;

import cc.towerdefence.minestom.lobby.LobbyExtension;
import cc.towerdefence.minestom.lobby.model.LobbyUser;
import cc.towerdefence.minestom.lobby.utils.PlayerObjectCache;
import cc.towerdefence.openmatch.frontend.client.OpenMatchClient;
import cc.towerdefence.openmatch.frontend.client.model.Ticket;
import cc.towerdefence.openmatch.frontend.model.TDMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemHideFlag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class QueueMenu {
    private static final ItemStack HOTBAR_ITEM = ItemStack.builder(Material.NETHER_STAR)
        .displayName(Component.text("Queue").decoration(TextDecoration.ITALIC, false))
        .meta(meta -> meta.enchantment(Enchantment.UNBREAKING, (short) 1).hideFlag(ItemHideFlag.HIDE_ENCHANTS))
        .build();

    private final @NotNull OpenMatchClient openMatchClient;
    private final @NotNull PlayerObjectCache<LobbyUser> userCache;

    public QueueMenu(@NotNull LobbyExtension extension) {
        this.openMatchClient = extension.getOpenMatchClient();
        this.userCache = extension.getUserCache();

        this.startGuiListener(extension);
    }

    private void createGui(@NotNull Player player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, Component.text("Queue Selector").decoration(TextDecoration.ITALIC, false));

        inventory.setItemStack(4, ItemStack.builder(Material.STONE_BRICKS).displayName(Component.text("Standard Queue").decoration(TextDecoration.ITALIC, false)).build());
        inventory.addInventoryCondition((target, slot, clickType, inventoryConditionResult) -> {
            inventoryConditionResult.setCancel(true);
            switch (slot) {
                case 4 -> {
                    this.queue(target, TDMode.STANDARD);
                }
            }
        });

        player.openInventory(inventory);
    }

    private void startGuiListener(Extension extension) {
        extension.getEventNode()
            .addListener(PlayerSpawnEvent.class, event -> event.getPlayer().getInventory().setItemStack(4, HOTBAR_ITEM))
            .addListener(PlayerUseItemEvent.class, event -> {
                if (event.getItemStack().getMaterial() == Material.NETHER_STAR)
                    this.createGui(event.getPlayer());
            });
    }

    private void queue(@NotNull Player player, @NotNull TDMode mode) {
        LobbyUser user = this.userCache.get(player);
        if (user.getTicket() != null) {
            this.deQueue(user);
            return;
        }
        this.openMatchClient.createTicket(mode).thenAccept(ticket -> {
            player.sendMessage(Component.text("Now in queue for " + mode.getStringMode(), NamedTextColor.GREEN));
            player.closeInventory();
            user.setTicket(ticket);
        });
    }

    private void deQueue(@NotNull LobbyUser user) {
        Ticket ticket = user.getTicket();
        this.openMatchClient.deleteTicket(ticket.getId()).thenAccept(unused -> {
            Player player = user.getPlayer();
            player.sendMessage(Component.text("Left " + TDMode.fromTags(ticket.getSearchFields().getTags()).getStringMode() + " queue", NamedTextColor.RED));
            player.closeInventory();
            user.setTicket(null);
        });
    }
}
