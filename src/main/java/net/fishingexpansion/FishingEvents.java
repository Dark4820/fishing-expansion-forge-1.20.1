package net.fishingexpansion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Runtime behaviour of the Fishing Expansion enchantments and of mob fishing.
 */
public class FishingEvents {
    /** NBT key put on a kelp item by the fishing loot table to mark "this catch is a live mob". */
    public static final String MOB_TAG = "FEMob";
    private static final String ENDER_USED_TAG = "fishing_expansion.ender_hook.used";

    // ------------------------------------------------------------------ helpers

    private static int handsLevel(LivingEntity entity, Enchantment enchantment) {
        return Math.max(
                EnchantmentHelper.getItemEnchantmentLevel(enchantment, entity.getMainHandItem()),
                EnchantmentHelper.getItemEnchantmentLevel(enchantment, entity.getOffhandItem()));
    }

    /** The item stack of a thrown trident, read from its saved data (the field itself is private). */
    private static ItemStack tridentStack(ThrownTrident trident) {
        CompoundTag tag = new CompoundTag();
        trident.saveWithoutId(tag);
        return tag.contains("Trident", Tag.TAG_COMPOUND) ? ItemStack.of(tag.getCompound("Trident")) : ItemStack.EMPTY;
    }

    private static int thunderingLevel(ThrownTrident trident) {
        ItemStack stack = tridentStack(trident);
        return stack.isEmpty() ? 0 : EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.THUNDERING.get(), stack);
    }

    private static void strike(ServerLevel world, Vec3 pos, @Nullable Entity cause) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
        if (bolt == null) return;
        bolt.moveTo(pos);
        if (cause instanceof ServerPlayer player) {
            bolt.setCause(player);
        }
        world.addFreshEntity(bolt);
        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
    }

    // ------------------------------------------------------------------ projectile impacts

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (projectile.level().isClientSide) return;
        HitResult hit = event.getRayTraceResult();
        if (hit.getType() == HitResult.Type.MISS) return;

        if (projectile instanceof FishingHook hook) {
            Player owner = hook.getPlayerOwner();
            if (!(owner instanceof ServerPlayer player)) return;
            if (hit instanceof EntityHitResult entityHit) {
                hookedStrike(hook, player, entityHit.getEntity());
            } else if (hit instanceof BlockHitResult blockHit) {
                enderHook(hook, player, blockHit);
            }
        } else if (projectile instanceof ThrownTrident trident && hit instanceof BlockHitResult blockHit) {
            thunderingOnLightningRod(trident, blockHit);
        }
    }

    /** Hooked Strike: the cast hook hurts the entity it hits. */
    private static void hookedStrike(FishingHook hook, ServerPlayer player, Entity target) {
        if (target == player || !(target instanceof LivingEntity living)) return;
        int level = handsLevel(player, ModEnchantments.HOOKED_STRIKE.get());
        if (level <= 0) return;
        living.hurt(player.damageSources().mobProjectile(hook, player), 2.0F * level);
    }

    /** Ender Hook: a hook that lands on dry ground teleports the player to it, at a cost. */
    private static void enderHook(FishingHook hook, ServerPlayer player, BlockHitResult hit) {
        if (hook.getTags().contains(ENDER_USED_TAG)) return;
        if (handsLevel(player, ModEnchantments.ENDER_HOOK.get()) <= 0) return;

        ServerLevel world = player.serverLevel();
        if (hook.isInWater() || !world.getFluidState(BlockPos.containing(hit.getLocation())).isEmpty()) return;
        hook.addTag(ENDER_USED_TAG);

        Direction face = hit.getDirection();
        Vec3 loc = hit.getLocation();
        double x = loc.x + face.getStepX() * 0.45D;
        double y = loc.y;
        double z = loc.z + face.getStepZ() * 0.45D;
        if (face == Direction.DOWN) {
            y -= player.getBbHeight();
        }

        AABB box = player.getBoundingBox().move(x - player.getX(), y - player.getY(), z - player.getZ());
        for (int up = 0; up < 4; up++) {
            if (world.noCollision(player, box.move(0.0D, up, 0.0D))) {
                double ty = y + up;
                world.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 1.0D, 0.5D, 0.0D);
                player.teleportTo(x, ty, z);
                player.fallDistance = 0.0F;
                world.sendParticles(ParticleTypes.PORTAL, x, ty + 1.0D, z, 60, 0.5D, 1.0D, 0.5D, 0.0D);
                world.playSound(null, x, ty, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.6F);

                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 1, false, false));
                player.hurt(player.damageSources().magic(), 2.0F);
                return;
            }
        }
    }

    /** Thundering: a trident that hits a lightning rod (with sky access) calls down lightning, storm or not. */
    private static void thunderingOnLightningRod(ThrownTrident trident, BlockHitResult hit) {
        if (!(trident.level() instanceof ServerLevel world)) return;
        BlockPos pos = hit.getBlockPos();
        if (!world.getBlockState(pos).is(Blocks.LIGHTNING_ROD)) return;
        if (!world.canSeeSky(pos.above()) || thunderingLevel(trident) <= 0) return;
        strike(world, Vec3.atBottomCenterOf(pos), trident.getOwner());
    }

    /** Thundering: a thrown trident that hurts something under open sky calls down lightning on it. */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel world)) return;
        if (!(event.getSource().getDirectEntity() instanceof ThrownTrident trident)) return;
        if (thunderingLevel(trident) <= 0 || !world.canSeeSky(victim.blockPosition())) return;
        strike(world, victim.position(), event.getSource().getEntity());
    }

    // ------------------------------------------------------------------ dolphin's grace

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || entity.tickCount % 5 != 0 || !entity.isSwimming()) return;
        ItemStack legs = entity.getItemBySlot(EquipmentSlot.LEGS);
        if (!legs.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DOLPHINS_GRACE.get(), legs) > 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 20, 0, false, false, true));
        }
    }

    // ------------------------------------------------------------------ entities joining the world

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        Entity entity = event.getEntity();

        if (entity instanceof ItemEntity item) {
            CompoundTag tag = item.getItem().getTag();
            if (tag != null && tag.contains(MOB_TAG, Tag.TAG_STRING)) {
                event.setCanceled(true);
                spawnFishedMob((ServerLevel) event.getLevel(), item, tag.getString(MOB_TAG));
            }
        } else if (entity instanceof AbstractArrow arrow && !(arrow instanceof ThrownTrident)
                && arrow.getOwner() instanceof LivingEntity shooter
                && handsLevel(shooter, ModEnchantments.WATER_TRAIL.get()) > 0) {
            WaterTrailManager.track(arrow);
            arrow.level().playSound(null, arrow.getX(), arrow.getY(), arrow.getZ(),
                    SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 2.0F);
        }
    }

    /**
     * Mob fishing: the fishing loot table marks some catches as kelp carrying {FEMob:"id"}.
     * When that item would enter the world, a real mob is created in its place and keeps the item's momentum,
     * so it flies towards the player like any other catch.
     */
    private static void spawnFishedMob(ServerLevel world, ItemEntity item, String mobId) {
        boolean withTrident = mobId.equals("drowned_trident");
        EntityType<?> type = EntityType.byString(withTrident ? "drowned" : mobId).orElse(null);
        if (type == null) return;

        Entity entity = type.create(world);
        if (entity == null) return;
        entity.moveTo(item.getX(), item.getY(), item.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
        entity.setDeltaMovement(item.getDeltaMovement());

        if (entity instanceof Mob mob) {
            mob.finalizeSpawn(world, world.getCurrentDifficultyAt(item.blockPosition()), MobSpawnType.EVENT, null, null);
            if (withTrident) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            }
        }
        world.addFreshEntity(entity);
    }
}
