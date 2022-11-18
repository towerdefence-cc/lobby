package cc.towerdefence.minestom.lobby.command;

import cc.towerdefence.minestom.lobby.LobbyModule;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class SpawnCommand extends Command {

    public SpawnCommand() {
        super("spawn");

        this.setCondition((sender, commandString) -> sender instanceof Player);
        this.setDefaultExecutor((sender, context) -> {
            ((Player) sender).teleport(LobbyModule.SPAWN_POS);
        });
    }
}
