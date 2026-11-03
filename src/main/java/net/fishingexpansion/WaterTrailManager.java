package net.fishingexpansion;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Water Trail: arrows shot from a bow with the enchantment leave water source blocks behind them,
 * which are removed again after one second. Only the arrows that carry the enchantment are tracked,
 * so the per-tick cost is zero for everything else.
 */
public class WaterTrailManager {
    private record Placed(ServerLevel level, BlockPos pos, long expiry) {}

    private static final int LIFETIME_TICKS = 20;
    private static final List<AbstractArrow> ARROWS = new ArrayList<>();
    private static final List<Placed> PLACED = new ArrayList<>();

    public static void track(AbstractArrow arrow) {
        ARROWS.add(arrow);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        if (ARROWS.isEmpty() && PLACED.isEmpty()) return;

        Iterator<AbstractArrow> arrows = ARROWS.iterator();
        while (arrows.hasNext()) {
            AbstractArrow arrow = arrows.next();
            if (arrow.level() != level) continue;
            if (arrow.isRemoved() || !arrow.isAlive() || isStuck(arrow)) {
                arrows.remove();
                continue;
            }
            Vec3 motion = arrow.getDeltaMovement();
            if (motion.lengthSqr() < 1.0E-4D) continue;

            BlockPos pos = BlockPos.containing(arrow.position().subtract(motion.normalize()));
            if (level.isEmptyBlock(pos)) {
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                PLACED.add(new Placed(level, pos, level.getGameTime() + LIFETIME_TICKS));
            }
        }

        long now = level.getGameTime();
        Iterator<Placed> placed = PLACED.iterator();
        while (placed.hasNext()) {
            Placed p = placed.next();
            if (p.level() != level) continue;
            if (now >= p.expiry()) {
                clear(p);
                placed.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for (Placed p : PLACED) clear(p);
        PLACED.clear();
        ARROWS.clear();
    }

    private static boolean isStuck(AbstractArrow arrow) {
        return arrow.tickCount > 2 && arrow.position().distanceToSqr(arrow.xo, arrow.yo, arrow.zo) < 1.0E-5D;
    }

    private static void clear(Placed p) {
        if (p.level().getBlockState(p.pos()).is(Blocks.WATER)) {
            p.level().setBlock(p.pos(), Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
