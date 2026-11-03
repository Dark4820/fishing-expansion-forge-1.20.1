package net.fishingexpansion;

import net.fishingexpansion.enchantment.FEEnchantment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    private static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, FishingExpansion.ID);

    private static final EquipmentSlot[] MAIN_HAND = {EquipmentSlot.MAINHAND};
    private static final EquipmentSlot[] HANDS = {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
    private static final EquipmentSlot[] LEGS = {EquipmentSlot.LEGS};

    /** Lure-like: -5 s of waiting time per level, up to V. Not compatible with vanilla Lure. */
    public static final RegistryObject<Enchantment> SEA_LURE = ENCHANTMENTS.register("sea_lure",
            () -> new FEEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.FISHING_ROD, HANDS,
                    5, 15, 9, 50, new ResourceLocation("minecraft", "lure")));

    /** Luck of the Sea-like: +1.5 luck per level, up to V. Not compatible with vanilla Luck of the Sea. */
    public static final RegistryObject<Enchantment> TREASURE_HUNTER = ENCHANTMENTS.register("treasure_hunter",
            () -> new FEEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.FISHING_ROD, HANDS,
                    5, 15, 9, 50, new ResourceLocation("minecraft", "luck_of_the_sea")));

    /** A cast hook that hits an entity hurts it (2 / 4 / 6 damage). */
    public static final RegistryObject<Enchantment> HOOKED_STRIKE = ENCHANTMENTS.register("hooked_strike",
            () -> new FEEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.FISHING_ROD, MAIN_HAND,
                    3, 15, 9, 50));

    /** A cast hook that lands on the ground teleports the player to it, with penalties. */
    public static final RegistryObject<Enchantment> ENDER_HOOK = ENCHANTMENTS.register("ender_hook",
            () -> new FEEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.FISHING_ROD, MAIN_HAND,
                    1, 15, 9, 50));

    /** Arrows leave a short-lived trail of water. Not compatible with Flame. */
    public static final RegistryObject<Enchantment> WATER_TRAIL = ENCHANTMENTS.register("water_trail",
            () -> new FEEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.BOW, MAIN_HAND,
                    1, 15, 9, 50, new ResourceLocation("minecraft", "flame")));

    /** Trident: lightning on hit (without a thunderstorm) and on lightning rods. */
    public static final RegistryObject<Enchantment> THUNDERING = ENCHANTMENTS.register("thundering",
            () -> new FEEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.TRIDENT, MAIN_HAND,
                    1, 25, 0, 25));

    /** Leggings: Dolphin's Grace while swimming. */
    public static final RegistryObject<Enchantment> DOLPHINS_GRACE = ENCHANTMENTS.register("dolphins_grace",
            () -> new FEEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.ARMOR_LEGS, LEGS,
                    1, 25, 25, 50));

    public static void register(IEventBus bus) {
        ENCHANTMENTS.register(bus);
    }
}
