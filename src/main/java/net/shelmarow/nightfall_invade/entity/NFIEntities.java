package net.shelmarow.nightfall_invade.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.spear_knight.Arterius;

public class NFIEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES;

    public static final RegistryObject<EntityType<Arterius>> ARTERIUS;

    static {
        ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NightFallInvade.MOD_ID);
        ARTERIUS = ENTITY_TYPES.register("arterius",()->EntityType.Builder.of(Arterius::new, MobCategory.MONSTER)
                        .sized(1f,2f).clientTrackingRange(64).setTrackingRange(64).fireImmune().build("arterius"));
    }

}

