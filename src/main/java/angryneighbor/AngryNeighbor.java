package angryneighbor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class AngryNeighbor implements ModInitializer {
    public static final String ID = "angryneighbor";
    public static final Identifier GRAB = new Identifier(ID, "grab");
    public static final EntityType<NeighborEntity> NEIGHBOR = Registry.register(Registries.ENTITY_TYPE, new Identifier(ID, "neighbor"),
        FabricEntityTypeBuilder.<NeighborEntity>createMob().spawnGroup(SpawnGroup.MONSTER)
            .entityFactory(NeighborEntity::new).dimensions(EntityDimensions.fixed(0.7f, 1.9f)).trackRangeBlocks(64).build());

    @Override public void onInitialize() {
        FabricDefaultAttributeRegistry.register(NEIGHBOR, NeighborEntity.attrs());
        Item egg = Registry.register(Registries.ITEM, new Identifier(ID, "neighbor_spawn_egg"),
            new SpawnEggItem(NEIGHBOR, 0x1A2A6C, 0xE8B27D, new FabricItemSettings()));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(e -> e.add(egg));
        ServerPlayNetworking.registerGlobalReceiver(GRAB, (server, player, handler, buf, rs) -> server.execute(() -> Game.grab(player)));
        CommandRegistrationCallback.EVENT.register((d, ra, env) -> Game.register(d));
    }
}
