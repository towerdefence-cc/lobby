package cc.towerdefence.minestom.lobby.menus;

import cc.towerdefence.minestom.lobby.LobbyExtension;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;

public class ServerSelectorMenu {
    private static final ItemStack HOTBAR_ITEM = ItemStack.builder(Material.COMPASS)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Server Selector"))
            .build();

    private static final ItemStack TOWER_DEFENCE_ITEM = ItemStack.builder(Material.STONE_BRICKS)
            .displayName(MiniMessage.miniMessage().deserialize("<gold>Tower Defence"))
            .build();

    private static final Inventory INVENTORY;

    static {
        INVENTORY = new Inventory(InventoryType.CHEST_3_ROW, "Server Selector");

        INVENTORY.setItemStack(13, TOWER_DEFENCE_ITEM);
    }

    public ServerSelectorMenu(LobbyExtension extension) {
        extension.getEventNode()
                .addListener(PlayerSpawnEvent.class, event -> event.getPlayer().getInventory().setItemStack(0, HOTBAR_ITEM))
                .addListener(InventoryPreClickEvent.class, event -> {
                    if (event.getClickType() == ClickType.DOUBLE_CLICK) return;

                    Player player = event.getPlayer();
                    if (event.getClickedItem() == HOTBAR_ITEM) {
                        player.openInventory(INVENTORY);
                        event.setCancelled(true);
                        return;
                    }

                    if (event.getClickedItem() == TOWER_DEFENCE_ITEM) {
                        //todo send to server or if in k8s get the server ip and port i guess?????
                        String serverName = "tower-defence";
                        event.setCancelled(true);
                        player.closeInventory();
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Connecting to <gold>" + serverName));

                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(serverName);
                        player.sendPacket(new PluginMessagePacket("BungeeCord", out.toByteArray()));
                    }
                });
    }
}
