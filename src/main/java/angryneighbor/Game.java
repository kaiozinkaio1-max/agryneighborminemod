package angryneighbor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.List;
import static net.minecraft.server.command.CommandManager.literal;

public class Game {
    static final String[] MODES = {"facil", "normal", "dificil"};

    public static void register(CommandDispatcher<ServerCommandSource> d) {
        d.register(literal("neighbor")
            .then(literal("start")
                .then(literal("easy").executes(c -> start(c, 0)))
                .then(literal("normal").executes(c -> start(c, 1)))
                .then(literal("hard").executes(c -> start(c, 2))))
            .then(literal("stop").executes(c -> { removeNeighbors(c.getSource().getPlayerOrThrow()); return 1; })));
    }

    static void removeNeighbors(ServerPlayerEntity p) {
        p.getServerWorld().getEntitiesByClass(NeighborEntity.class, p.getBoundingBox().expand(80), e -> true).forEach(NeighborEntity::discard);
    }

    static int start(CommandContext<ServerCommandSource> c, int mode) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayerEntity p = c.getSource().getPlayerOrThrow();
        ServerWorld w = p.getServerWorld();
        removeNeighbors(p);
        int x0 = p.getBlockX() - 7, z0 = p.getBlockZ() + 5, y0 = p.getBlockY() - 1;
        // fundacao + limpa o volume
        for (int dx = 0; dx <= 14; dx++) for (int dz = 0; dz <= 14; dz++) {
            for (int y = y0 - 3; y < y0; y++) set(w, x0 + dx, y, z0 + dz, Blocks.COBBLESTONE);
            set(w, x0 + dx, y0, z0 + dz, Blocks.OAK_PLANKS);
            for (int y = y0 + 1; y <= y0 + 6; y++) set(w, x0 + dx, y, z0 + dz, Blocks.AIR);
            set(w, x0 + dx, y0 + 5, z0 + dz, Blocks.DARK_OAK_PLANKS); // teto
        }
        for (int dx = 0; dx <= 14; dx++) for (int dz = 0; dz <= 14; dz++) {
            boolean wall = dx == 0 || dx == 14 || dz == 0 || dz == 14;
            boolean part1 = dz == 7 && dx > 0 && dx < 14 && !(dx >= 3 && dx <= 4) && !(dx >= 10 && dx <= 11);
            boolean part2 = dx == 7 && dz > 7 && dz < 14 && !(dz >= 10 && dz <= 11);
            if (!(wall || part1 || part2)) continue;
            for (int y = y0 + 1; y <= y0 + 4; y++) {
                boolean win = wall && y >= y0 + 2 && y <= y0 + 3 && ((dz == 14 && (dx == 3 || dx == 11)) || ((dx == 0 || dx == 14) && (dz == 4 || dz == 10)));
                set(w, x0 + dx, y, z0 + dz, win ? Blocks.GLASS : Blocks.STONE_BRICKS);
            }
        }
        for (int dx = 6; dx <= 8; dx++) for (int y = y0 + 1; y <= y0 + 3; y++) set(w, x0 + dx, y, z0, Blocks.AIR); // porta
        int[][] lamps = {{2, 2}, {12, 2}, {2, 12}, {12, 12}, {5, 5}, {9, 5}, {10, 9}};
        for (int[] l : lamps) set(w, x0 + l[0], y0 + 1, z0 + l[1], Blocks.LANTERN);

        // chaves e itens (so pegos com F)
        int[][] keys = {{3, 11}, {11, 12}, {7, 3}};
        for (int[] k : keys) {
            ItemStack s = new ItemStack(Items.TRIPWIRE_HOOK); s.setCustomName(Text.literal("Chave"));
            drop(w, x0 + k[0], y0 + 1, z0 + k[1], s);
        }
        drop(w, x0 + 2, y0 + 1, z0 + 5, new ItemStack(Items.APPLE));
        drop(w, x0 + 12, y0 + 1, z0 + 5, new ItemStack(Items.BREAD));
        drop(w, x0 + 5, y0 + 1, z0 + 9, new ItemStack(Items.GOLDEN_CARROT));
        drop(w, x0 + 10, y0 + 1, z0 + 10, new ItemStack(Items.COOKED_BEEF));

        NeighborEntity n = AngryNeighbor.NEIGHBOR.create(w);
        n.refreshPositionAndAngles(x0 + 7.5, y0 + 1, z0 + 11.5, 0, 0);
        n.applyMode(mode); n.setPersistent();
        w.spawnEntity(n);
        p.sendMessage(Text.literal("Modo " + MODES[mode] + "! Entre na casa, pegue as 3 CHAVES com [F] (perto do item) e fuja do vizinho!"), false);
        return 1;
    }

    static void set(ServerWorld w, int x, int y, int z, net.minecraft.block.Block b) { w.setBlockState(new BlockPos(x, y, z), b.getDefaultState(), 3); }

    static void drop(ServerWorld w, int x, int y, int z, ItemStack s) {
        ItemEntity e = new ItemEntity(w, x + 0.5, y + 0.3, z + 0.5, s);
        e.setVelocity(Vec3d.ZERO); e.setPickupDelay(32767); e.setNeverDespawn();
        w.spawnEntity(e);
    }

    public static void grab(ServerPlayerEntity p) {
        ServerWorld w = p.getServerWorld();
        List<ItemEntity> l = w.getEntitiesByClass(ItemEntity.class, p.getBoundingBox().expand(3.5), e -> true);
        ItemEntity best = null; double bd = 1e9;
        for (ItemEntity e : l) { double d = e.squaredDistanceTo(p); if (d < bd) { bd = d; best = e; } }
        if (best == null) return;
        best.setPickupDelay(0); best.onPlayerCollision(p);
        int keys = 0;
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack st = p.getInventory().getStack(i);
            if (st.isOf(Items.TRIPWIRE_HOOK) && st.hasCustomName()) keys += st.getCount();
        }
        if (keys >= 3) {
            removeNeighbors(p);
            p.sendMessage(Text.literal("VOCE PEGOU AS 3 CHAVES E ESCAPOU DO VIZINHO!"), false);
        } else p.sendMessage(Text.literal("Chaves: " + keys + "/3"), true);
    }
}
