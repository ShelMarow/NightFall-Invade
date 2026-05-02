package net.shelmarow.nightfall_invade.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shelmarow.nightfall_invade.NightFallInvade;
import net.shelmarow.nightfall_invade.entity.blood_hunter.ScarletHunter;
import net.shelmarow.nightfall_invade.entity.fallen_knight.FallenKnight;
import net.shelmarow.nightfall_invade.entity.misc.blood_bomb.BloodBoom;
import net.shelmarow.nightfall_invade.entity.misc.blood_slash.BloodSlashEntity;
import net.shelmarow.nightfall_invade.entity.arterius.Arterius;

public class NFIEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NightFallInvade.MOD_ID);

    public static final RegistryObject<EntityType<Arterius>> ARTERIUS = ENTITY_TYPES.register("arterius",()->
            EntityType.Builder.of(Arterius::new, MobCategory.MONSTER).sized(1f,2f)
                    .clientTrackingRange(64).setTrackingRange(64)
                    .updateInterval(1).fireImmune().build("arterius"));

    public static final RegistryObject<EntityType<ScarletHunter>> SCARLET_HUNTER = ENTITY_TYPES.register("scarlet_hunter",()->
            EntityType.Builder.of(ScarletHunter::new, MobCategory.MISC).sized(1.1f,2.25f)
                    .clientTrackingRange(64).setTrackingRange(64).updateInterval(1).fireImmune().build("scarlet_hunter"));

    public static final RegistryObject<EntityType<FallenKnight>> FALLEN_KNIGHT = ENTITY_TYPES.register("fallen_knight",()->
            EntityType.Builder.of(FallenKnight::new, MobCategory.MONSTER).sized(0.75F, 2F)
                    .build("fallen_knight"));

    public static final RegistryObject<EntityType<BloodBoom>> BLOOD_BOOM = ENTITY_TYPES.register("blood_boom",()->
            EntityType.Builder.<BloodBoom>of(BloodBoom::new, MobCategory.MISC).sized(0.25F,0.25F)
                    .noSave().clientTrackingRange(64).setTrackingRange(64)
                    .updateInterval(1).fireImmune().build("blood_boom"));


    public static final RegistryObject<EntityType<BloodSlashEntity>> BLOOD_SLASH =
            ENTITY_TYPES.register("vacuum_slice", () ->
                    EntityType.Builder.<BloodSlashEntity>of(BloodSlashEntity::new, MobCategory.MISC)
                            .sized(0F,0F).noSave().build("vacuum_slice"));
}

