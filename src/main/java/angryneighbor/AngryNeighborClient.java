package angryneighbor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AngryNeighborClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        EntityRendererRegistry.register(AngryNeighbor.NEIGHBOR, NeighborRenderer::new);
        KeyBinding grab = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.angryneighbor.grab", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F, "category.angryneighbor"));
        ClientTickEvents.END_CLIENT_TICK.register(c -> { while (grab.wasPressed()) ClientPlayNetworking.send(AngryNeighbor.GRAB, PacketByteBufs.create()); });
    }
}
