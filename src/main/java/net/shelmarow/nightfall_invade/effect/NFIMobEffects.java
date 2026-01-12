package net.shelmarow.nightfall_invade.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shelmarow.nightfall_invade.NightFallInvade;

public class NFIMobEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, NightFallInvade.MOD_ID);

    public static final RegistryObject<MobEffect> SOUL_OF_FLAME =
            EFFECTS.register("soul_of_flame",() -> new SoulOfFlameEffect()
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,"f77121cf-e862-4356-9008-5a4f9a9c6388",-0.05D, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED,"f44d84ed-3915-434e-87ac-0ef23f7a11a5",-0.05D, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,"00447739-2175-48fd-9538-dedc0c5b6828",-0.05D, AttributeModifier.Operation.MULTIPLY_TOTAL)
            );

}
