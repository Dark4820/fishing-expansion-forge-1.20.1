package net.fishingexpansion;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FishingExpansion.ID)
public class FishingExpansion {
    public static final String ID = "fishing_expansion";

    public FishingExpansion() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEnchantments.register(bus);

        MinecraftForge.EVENT_BUS.register(FishingEvents.class);
        MinecraftForge.EVENT_BUS.register(WaterTrailManager.class);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }
}
