package net.fishingexpansion.enchantment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

/**
 * Generic enchantment used by every Fishing Expansion enchantment. The behaviour itself lives in
 * FishingEvents / WaterTrailManager / EnchantmentHelperMixin; this class only describes the enchantment
 * (levels, costs, compatibility) and keeps it out of the enchanting table and villager trades, so that it is
 * obtained by fishing, like in the original datapack.
 */
public class FEEnchantment extends Enchantment {
    private final int maxLevel;
    private final int minBase;
    private final int minPerLevel;
    private final int range;
    private final Set<ResourceLocation> incompatible;

    public FEEnchantment(Enchantment.Rarity rarity, EnchantmentCategory category, EquipmentSlot[] slots,
                         int maxLevel, int minBase, int minPerLevel, int range, ResourceLocation... incompatible) {
        super(rarity, category, slots);
        this.maxLevel = maxLevel;
        this.minBase = minBase;
        this.minPerLevel = minPerLevel;
        this.range = range;
        this.incompatible = Set.of(incompatible);
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getMinCost(int level) {
        return minBase + (level - 1) * minPerLevel;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + range;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(other);
        return super.checkCompatibility(other) && (key == null || !incompatible.contains(key));
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }
}
