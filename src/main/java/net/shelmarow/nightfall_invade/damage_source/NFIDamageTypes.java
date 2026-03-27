package net.shelmarow.nightfall_invade.damage_source;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.shelmarow.nightfall_invade.NightFallInvade;

public class NFIDamageTypes {

    public static final ResourceKey<DamageType> BLOOD_SLASH =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NightFallInvade.MOD_ID, "blood_slash"));

}
