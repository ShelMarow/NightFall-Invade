package net.shelmarow.nightfall_invade.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shelmarow.nightfall_invade.NightFallInvade;

public class NFIParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, NightFallInvade.MOD_ID);


    public static final RegistryObject<SimpleParticleType> BLOOD_A =
            PARTICLE_TYPES.register("blood_a", () -> new SimpleParticleType(false));


    public static final RegistryObject<SimpleParticleType> BLOOD_B =
            PARTICLE_TYPES.register("blood_b", () -> new SimpleParticleType(false));

}
