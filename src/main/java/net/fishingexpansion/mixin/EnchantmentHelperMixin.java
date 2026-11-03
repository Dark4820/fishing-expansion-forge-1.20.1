package net.fishingexpansion.mixin;

import net.fishingexpansion.ModEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The fishing hook reads its bite speed (Lure) and luck (Luck of the Sea) from these two helpers when the rod is cast.
 * Sea Lure and Treasure Hunter just add their own bonus on top.
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(method = "getFishingSpeedBonus", at = @At("RETURN"), cancellable = true)
    private static void fishingexpansion$seaLure(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SEA_LURE.get(), stack);
        if (level > 0) {
            cir.setReturnValue(cir.getReturnValue() + level);
        }
    }

    @Inject(method = "getFishingLuckBonus", at = @At("RETURN"), cancellable = true)
    private static void fishingexpansion$treasureHunter(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TREASURE_HUNTER.get(), stack);
        if (level > 0) {
            cir.setReturnValue(cir.getReturnValue() + (int) Math.round(1.5D * level));
        }
    }
}
