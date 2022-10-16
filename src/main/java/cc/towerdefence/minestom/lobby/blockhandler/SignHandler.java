package cc.towerdefence.minestom.lobby.blockhandler;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class SignHandler implements BlockHandler {
    private static final NamespaceID NAMESPACE_ID = NamespaceID.from(Key.key("minecraft:sign"));

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return NAMESPACE_ID;
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.Byte("GlowingText"),
                Tag.String("Color"),
                Tag.String("Text1"),
                Tag.String("Text2"),
                Tag.String("Text3"),
                Tag.String("Text4")
        );
    }

    public static void register() {
        MinecraftServer.getBlockManager().registerHandler(SignHandler.NAMESPACE_ID, SignHandler::new);
    }
}
